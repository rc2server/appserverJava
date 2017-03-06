package edu.wvu.stat.rc2.ws;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wvu.stat.rc2.persistence.Rc2DataSourceFactory;
import edu.wvu.stat.rc2.rworker.RWorker;
import edu.wvu.stat.rc2.rworker.response.BaseRResponse;
import edu.wvu.stat.rc2.ws.SessionError.SessionErrorException;
import edu.wvu.stat.rc2.ws.request.*;
import edu.wvu.stat.rc2.ws.response.BaseResponse;
import edu.wvu.stat.rc2.ws.response.ErrorResponse;
import edu.wvu.stat.rc2.ws.response.FileChangedResponse;
import edu.wvu.stat.rc2.ws.response.FileChangedResponse.ChangeType;
import edu.wvu.stat.rc2.ws.response.FileOperationResponse;
import edu.wvu.stat.rc2.ws.response.SaveResponse;
import edu.wvu.stat.rc2.config.SessionConfig;
import edu.wvu.stat.rc2.persistence.RCFile;
import edu.wvu.stat.rc2.persistence.RCFileQueries;
import edu.wvu.stat.rc2.persistence.RCSessionRecord;
import edu.wvu.stat.rc2.persistence.RCWorkspace;
import edu.wvu.stat.rc2.persistence.RCWorkspaceQueries;
import edu.wvu.stat.rc2.persistence.Rc2DAO;

//TODO: add shutdown hook for rworker
//		handle prematurely closed websocket


@SuppressWarnings("unused")
public final class RCSession implements RCSessionSocket.Delegate, RWorker.Delegate, 
	FileUpdateCenter.Delegate, Rc2DataSourceFactory.NotificationListener
{
	static final Logger log = LoggerFactory.getLogger("rc2.RCSession");

	private final Rc2DataSourceFactory _dbfactory;
	private RCWorkspace _wspace;
	private final WebSocketCollection _webSockets;
	private ObjectMapper _mapper;
	private ObjectMapper _msgPackMapper;
	private Rc2DAO _dao;
	private RWorker _rworker;
	private SessionConfig _config;
	private ExecutorService _executor;
	private final long _startTime;
	private final int _sessionId;
	/** who has requested being kept up-to-date on variable changes */
	private List<RCSessionSocket> _variableWatchers;
	private FileUpdateCenter _fileUpdater;
	
	/**
	 @param dbfactory A factory is passed so that if the connection is dropped for some reason, a new one can be opened.
	 @param workspace The workspace this session represents.
	 @param mapper An object mapper to use for json conversion. If null, a generic mapper will be created.
	 @param rworker The rworker to use. If null, one will be created.
	 */
	RCSession(Rc2DataSourceFactory dbfactory, ObjectMapper mapper, 
			SessionConfig config, int wspaceId, RWorker rworker) 
	{
		this(dbfactory, mapper, config, wspaceId, rworker, Executors.newSingleThreadExecutor());
	}

	/**
	 @param dbfactory A factory is passed so that if the connection is dropped for some reason, a new one can be opened.
	 * @param mapper An object mapper to use for json conversion. If null, a generic mapper will be created.
	 * @param rworker The rworker to use. If null, one will be created.
	 * @param executor Used for background processing of work.
	 * @param workspace The workspace this session represents.
	 */
	RCSession(Rc2DataSourceFactory dbfactory, ObjectMapper mapper, 
			SessionConfig config, int wspaceId, RWorker rworker, ExecutorService executor) 
	{
		_dbfactory = dbfactory;
		_mapper = mapper;
		_config = config;
		if (null == _mapper)
			_mapper = new ObjectMapper();
		_executor = executor;
		_msgPackMapper = new ObjectMapper(new MessagePackFactory());
		_dao = _dbfactory.createDAO();
		_wspace = _dao.findWorkspaceById(wspaceId);
		if (null == _wspace)
			throw new IllegalArgumentException("invalid workspaceId");
		
		_webSockets = new WebSocketCollection(_mapper, _msgPackMapper);
		_startTime = System.currentTimeMillis();
		_variableWatchers = new ArrayList<RCSessionSocket>();
		
		//must generate before rworker thread is started
		RCSessionRecord.Queries srecDao = _dao.getDBI().onDemand(RCSessionRecord.Queries.class);
		_sessionId = srecDao.createSessionRecord(_wspace.getId());

		_rworker = rworker;
		if (null == _rworker)
			_rworker = new RWorker(new RWorker.SocketFactory(getSessionConfig().getRComputeHost()), this);
		else
			_rworker.setDelegate(this);
		Thread rwt = new Thread(_rworker);
		rwt.setName("rworker " + wspaceId);
		rwt.start();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> _rworker.shutdown()));
		_dbfactory.addNotificationListener("rcfile", this);
		_fileUpdater = new FileUpdateCenter(this, _executor);
	}

	//RCSessionSocket.Delegate
	public int getWorkspaceId() { return _wspace.getId(); }
	public RCWorkspace getWorkspace() { return _wspace; }

	public int getSessionRecordId() { return _sessionId; }
	public ObjectMapper getObjectMapper() { return _mapper; }
	public int getClientCount() { return _webSockets.size(); }
	public boolean isIdle() { return true; }
	
	void shutdown() {
		_rworker.shutdown();
		RCSessionRecord.Queries srecDao = _dao.getDBI().onDemand(RCSessionRecord.Queries.class);
		srecDao.closeSessionRecord(_sessionId);
	}
	
	@Override
	public void handleNotification(String channelName, String message) {
		if (!channelName.equals("rcfile") || message.length() < 2)
			return;
		_fileUpdater.databaseFileUpdated(message, _rworker);
	}
	
	private BiFunction<RCFile, FileRequest, CompletableFuture<RCFile>> 
		functionForOperation(FileRequest.FileOperation operation)
	{
		switch (operation) {
			case REMOVE:
				return this::removeFile;
			case RENAME:
				return this::renameFile;
			case DUPLICATE:
				return this::duplicateFile;
			default:
				log.error("impossible to have an unkown operation");
				throw new RuntimeException("illegal state");
		}
	}
	
	private void handleFileRequest(FileRequest request, RCSessionSocket socket) {
		RCFile inFile = _dao.getFileDao().findById(request.getFileId());
		SessionError error = null;
		if (null == inFile) {
			error = new SessionError(SessionError.ErrorCode.NoSuchFile);
		} else if (request.getFileVersion() != inFile.getVersion()){
			error = new SessionError(SessionError.ErrorCode.VersionMismatch);
		} else {
			functionForOperation(request.getOperation())
				.apply(inFile, request)
				.thenAccept((returnedFile) -> { 
					broadcastToAllClients(new FileOperationResponse(request.getTransId(), 
							request.getOperation(), true, returnedFile, null));
				})
				.exceptionally((e) -> { 
					if (e instanceof SessionErrorException) {
						broadcastToAllClients(new FileOperationResponse(request.getTransId(), 
								request.getOperation(), false, null, ((SessionErrorException) e).getError()));
					} else {
						log.error("unknown exception from file operation");
						broadcastToAllClients(new FileOperationResponse(request.getTransId(), 
								request.getOperation(), false, null, 
								new SessionError(SessionError.ErrorCode.UnknownError)));
					}
					return null;
				});
			
		}
		if (error != null) {
			broadcastToAllClients(new FileOperationResponse(request.getTransId(), 
					request.getOperation(), false, null, error));
			return;
		}
	}
	
	private void handleExecuteRequest(ExecuteRequest request, RCSessionSocket socket) {
		//TODO: implement sourcing via request.getType()
		if (request.getFileId() > 0) {
			_rworker.executeScriptFile(request.getFileId());
		} else {
			_rworker.executeScript(request.getCode(), request.getNoEcho());
		}
	}

	private void handleGetVariableRequest(GetVariableRequest request, RCSessionSocket socket) {
		_rworker.fetchVariableValue(request.getVariable());
	}

	private void handleHelpRequest(HelpRequest request, RCSessionSocket socket) {
		_rworker.lookupInHelp(request.getTopic());
	}

	private void handleWatchVariablesRequest(WatchVariablesRequest request, RCSessionSocket socket) {
		if (request.getWatch()) {
			_variableWatchers.add(socket);
			_rworker.setWatchingVariables(true);
		} else {
			stopWatchingVariables(socket);
		}
	}

	private void handleKeepAliveRequest(KeepAliveRequest request, RCSessionSocket socket) {
		//do nothing
	}
	
	private void handleUserListRequest(UserListRequest request, RCSessionSocket socket) {
		
	}

	private void handleSaveRequest(SaveRequest request, RCSessionSocket socket) {
		if (request == null) { log.warn("save request is null"); }
		SessionError error = null;
		RCFileQueries fdao = getDAO().getFileDao();
		log.info("save request=" + request + " fid=" + request.getFileId() + " ver=" + request.getFileVersion());
		RCFile file = fdao.findById(request.getFileId());
		log.info("saved version = " + file.getVersion());
		if (null == file) {
			error = new SessionError(SessionError.ErrorCode.NoSuchFile);
		} else if (file.getVersion() != request.getFileVersion()) {
			log.info("file version mismatch:" + file.getVersion() + " vs " + request.getFileVersion());
			error = new SessionError(SessionError.ErrorCode.VersionMismatch);
		} else {
			//do the save, which will trigger notification
			ByteArrayInputStream stream = new ByteArrayInputStream(request.getContent().getBytes(Charset.forName("utf-8")));
			file = fdao.updateFileContents(request.getFileId(), stream);
			if (file == null) {
				error = new SessionError(SessionError.ErrorCode.DatabaseUpdateFailed);
			}
		}
		if (error != null)
			file = null;
		SaveResponse rsp = new SaveResponse(request.getTransId(), error == null, file, error);
		broadcastToSingleClient(rsp, socket.getSocketId());
	}
	
	private void stopWatchingVariables(RCSessionSocket socket) {
		_variableWatchers.remove(socket);
		if (_variableWatchers.size() < 1)
			_rworker.setWatchingVariables(false);
	}
	
	private void refetchFiles() {
		_wspace.setFiles(_dao.getFileDao().filesForWorkspaceId(_wspace.getId()));
	}
	
	private CompletableFuture<RCFile> removeFile(RCFile file, FileRequest request) {
		log.debug("removeFile: " + file.getId());
		CompletableFuture<RCFile> future = new CompletableFuture<>();
		_fileUpdater.addDeleteCallback(file.getId(), file.getVersion(), (aFile) -> {
			log.info("removeFile callback triggered");
			future.complete(null);
		});
		_executor.submit(() -> { 
			if (_dao.getFileDao().deleteFile(file.getId()) != 1) {
				future.completeExceptionally(new SessionErrorException(
						new SessionError(SessionError.ErrorCode.DatabaseUpdateFailed)));
			}
		} );
		return future;
	}

	CompletableFuture<RCFile> renameFile(RCFile file, FileRequest request) {
		log.debug("renameFile: " + file.getId());
		CompletableFuture<RCFile> future = new CompletableFuture<>();
		_fileUpdater.addUpdateCallback(file.getId(), file.getVersion(), (aFile) -> {
			future.complete(aFile);
		});
		_executor.submit(() -> { 
			RCFile updatedFile = _dao.getFileDao().updateFileName(file.getId(), request.getNewName());
			if (null == updatedFile) {
				future.completeExceptionally(new SessionErrorException(
						new SessionError(SessionError.ErrorCode.DatabaseUpdateFailed)));
			}
		} );
		return future;
	}

	private CompletableFuture<RCFile> duplicateFile(RCFile file, FileRequest request) {
		log.debug("duplicateFile: " + file.getId());
		CompletableFuture<RCFile> future = new CompletableFuture<>();
		_fileUpdater.addInsertCallback(request.getNewName(), (aFile) -> {
			future.complete(aFile);
		});
		_executor.submit(() -> { 
			RCFile newFile = _dao.getFileDao().duplicateFile(file, request.getNewName());
			if (null == newFile) {
				future.completeExceptionally(new SessionErrorException(
						new SessionError(SessionError.ErrorCode.DatabaseUpdateFailed)));
			}
		} );
		return future;
	}

	//RCSessionSocket.Delegate
	@Override
	public void websocketUseDatabaseHandle(HandleCallback<Void> callback) {
		_dao.getDBI().withHandle(callback);
	}

	//RCSessionSocket.Delegate
	@Override
	public Map<String, Object> getSessionDescriptionForWebsocket(RCSessionSocket socket) {
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("workspace", _wspace);
		return map;
	}

	//RCSessionSocket.Delegate
	@Override
	public void websocketOpened(RCSessionSocket socket) {
		_webSockets.add(socket);
	}

	//RCSessionSocket.Delegate
	@Override
	public void websocketClosed(RCSessionSocket socket) {
		log.info("websocket says to close");
		_webSockets.remove(socket);
		stopWatchingVariables(socket);
		_rworker.saveEnvironment();
	}

	//RCSessionSocket.Delegate
	@Override
	public void processWebsocketMessage(RCSessionSocket socket, String msg) {
		BaseRequest req=null;
		String cmdStr = null;
		try {
			req = _mapper.readValue(msg, BaseRequest.class);
			final String methodName = "handle" + req.getClass().getSimpleName();
			Method m = getClass().getDeclaredMethod(methodName, req.getClass(), RCSessionSocket.class);
			m.invoke(this, req, socket);
		} catch (Exception e) {
			log.error("error parsing client json", e);
			broadcastToAllClients(new ErrorResponse("unknown error"));
		}
	}

	//RCSessionSocket.Delegate
	@Override
	public void processWebsocketBinaryMessage(RCSessionSocket socket, byte[] data, int offset, int length) {
		log.info("got binary message");
		byte[] buffer = Arrays.copyOfRange(data, offset, length + offset);
		BaseRequest req=null;
		String cmdStr = null;
		try {
			req =  _msgPackMapper.readValue(buffer, BaseRequest.class);
			final String methodName = "handle" + req.getClass().getSimpleName();
			Method m = getClass().getDeclaredMethod(methodName, req.getClass(), RCSessionSocket.class);
			m.invoke(this, req, socket);
		} catch (Exception e) {
			log.error("error parsing client binary request", e);
			broadcastToSingleClient(new ErrorResponse("failed to process binary message"), socket.getSocketId());
		}
	}

	//RWorker.Delegate
	@Override
	public Rc2DAO getDAO() {
		return _dao;
	}

	//RWorker.Delegate
	@Override
	public SessionConfig getSessionConfig() { return _config; }
	
	//RWorker.Delegate
	@Override
	public void broadcastToAllClients(BaseResponse response) {
		_webSockets.broadcastToAllClients(response);
	}

	//RWorker.Delegate
	@Override
	public void broadcastToSingleClient(BaseResponse response, int socketId) {
		_webSockets.broadcastToSingleClient(response, socketId);
	}

	//RWorker.Delegate
	@Override
	public void clientHadError(Exception e) {
		log.warn("client had error", e);
		//TODO: handle better
	}

	//RWorker.Delegate
	@Override
	public void connectionFailed(Exception e) {
		log.error("connection to rcompute server failed");
		//TODO: handle better
	}

	enum MessageKeys {
		msg, status, user, fname, sid
	};
	
	private static final String MsgKey_msg = "msg";
	private static final String MsgKey_status = "status";
	private static final String MsgKey_user = "user";
	private static final String MsgKey_fname = "fname";
	private static final String MsgKey_sid = "sid";

}
