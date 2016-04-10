package edu.wvu.stat.rc2.ws;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import edu.wvu.stat.rc2.ws.request.*;
import edu.wvu.stat.rc2.ws.response.BaseResponse;
import edu.wvu.stat.rc2.ws.response.ErrorResponse;
import edu.wvu.stat.rc2.ws.response.FileChangedResponse;
import edu.wvu.stat.rc2.ws.response.FileChangedResponse.ChangeType;
import edu.wvu.stat.rc2.ws.response.SaveResponse;
import edu.wvu.stat.rc2.persistence.RCFile;
import edu.wvu.stat.rc2.persistence.RCFileQueries;
import edu.wvu.stat.rc2.persistence.RCSessionRecord;
import edu.wvu.stat.rc2.persistence.RCWorkspace;
import edu.wvu.stat.rc2.persistence.RCWorkspaceQueries;
import edu.wvu.stat.rc2.persistence.Rc2DAO;

//TODO: add shutdown hook for rworker
//		handle prematurely closed websocket


@SuppressWarnings("unused")
public final class RCSession implements RCSessionSocket.Delegate, RWorker.Delegate, Rc2DataSourceFactory.NotificationListener
{
	static final Logger log = LoggerFactory.getLogger("rc2.RCSession");

	private final Rc2DataSourceFactory _dbfactory;
	private RCWorkspace _wspace;
	private final List<RCSessionSocket> _webSockets;
	private ObjectMapper _mapper;
	private ObjectMapper _msgPackMapper;
	private Rc2DAO _dao;
	private RWorker _rworker;
	private ExecutorService _executor;
	private final long _startTime;
	private final int _sessionId;
	private boolean _watchingVariables;
	
	/**
	 @param dbfactory A factory is passed so that if the connection is dropped for some reason, a new one can be opened.
	 @param workspace The workspace this session represents.
	 @param mapper An object mapper to use for json conversion. If null, a generic mapper will be created.
	 @param rworker The rworker to use. If null, one will be created.
	 */
	RCSession(Rc2DataSourceFactory dbfactory, ObjectMapper mapper, int wspaceId, RWorker rworker) 
	{
		_dbfactory = dbfactory;
		_mapper = mapper;
		if (null == _mapper)
			_mapper = new ObjectMapper();
		_executor = Executors.newSingleThreadExecutor();
		_msgPackMapper = new ObjectMapper(new MessagePackFactory());
		_dao = _dbfactory.createDAO();
		_wspace = _dao.findWorkspaceById(wspaceId);
		if (null == _wspace)
			throw new IllegalArgumentException("invalid workspaceId");
		
		_webSockets = new ArrayList<RCSessionSocket>();
		_startTime = System.currentTimeMillis();
		
		//must generate before rworker thread is started
		RCSessionRecord.Queries srecDao = _dao.getDBI().onDemand(RCSessionRecord.Queries.class);
		_sessionId = srecDao.createSessionRecord(_wspace.getId());

		_rworker = rworker;
		if (null == _rworker)
			_rworker = new RWorker(new RWorker.SocketFactory(), this);
		else
			_rworker.setDelegate(this);
		Thread rwt = new Thread(_rworker);
		rwt.setName("rworker " + wspaceId);
		rwt.start();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> _rworker.shutdown()));
		_dbfactory.addNotificationListener("rcfile", this);
	}

	//RCSessionSocket.Delegate
	public int getWorkspaceId() { return _wspace.getId(); }

	public int getSessionRecordId() { return _sessionId; }
	public ObjectMapper getObjectMapper() { return _mapper; }
	public int getClientCount() {
		return _webSockets.size();
	}
	public boolean isIdle() { return true; }
	
	
	void shutdown() {
		log.info("session shutting down", new Exception());
		_rworker.shutdown();
		RCSessionRecord.Queries srecDao = _dao.getDBI().onDemand(RCSessionRecord.Queries.class);
		srecDao.closeSessionRecord(_sessionId);
	}
	
	@Override
	public void handleNotification(String channelName, String message) {
		if (!channelName.equals("rcfile") || message.length() < 2)
			return;
		log.info("got db file notification:" + message);
		//force refresh from the database. ideally we should be more selective, but the performance likely doesn't matter
		_wspace.setFiles(_dao.getFileDao().filesForWorkspaceId(_wspace.getId()));
		String[] parts = message.split("/");
		int fid = Integer.parseInt(parts[0].substring(1));
		Optional<RCFile> file = _wspace.getFileWithId(fid);
		if (!file.isPresent()) {
			log.warn("got file notification '" + message + "' for unknown file in workspace " + _wspace.getId());
			return;
		}
		ChangeType ctype = null;
		switch(message.charAt(0)) {
			case 'd':
				ctype = ChangeType.Delete;
				break;
			case 'i':
				ctype = ChangeType.Insert;
				break;
			case 'u':
				ctype = ChangeType.Update;
				break;
			default:
				log.error("file note with invalid operation: " + message);
				return;
		}
		FileChangedResponse  rsp = new FileChangedResponse(file.get(), ctype);
		broadcastToAllClients(rsp);
		_executor.submit(() -> {
			log.info("telling worker file was updated");
			_rworker.fileUpdated(file.get());
		});
	}
		
	private void handleExecuteRequest(ExecuteRequest request, RCSessionSocket socket) {
		//TODO: implement sourcing via request.getType()
		if (request.getFileId() > 0) {
			_rworker.executeScriptFile(request.getFileId());
		} else {
			_rworker.executeScript(request.getCode());
		}
	}

	private void handleGetVariableRequest(GetVariableRequest request, RCSessionSocket socket) {
		_rworker.fetchVariableValue(request.getVariable());
	}

	private void handleHelpRequest(HelpRequest request, RCSessionSocket socket) {
		_rworker.lookupInHelp(request.getTopic());
	}

	private void handleWatchVariablesRequest(WatchVariablesRequest request, RCSessionSocket socket) {
		
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
			log.info("saving file modifications");
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
	public void broadcastToAllClients(BaseResponse response) {
		try {
			Object serializedResponse;
			if (response.isBinaryMessage())
				serializedResponse = _msgPackMapper.writeValueAsBytes(response);
			else
				serializedResponse = _mapper.writeValueAsString(response);
			_webSockets.forEach(socket -> {
				try {
					socket.sendResponse(serializedResponse);
				} catch (Exception e) {
					log.info("error sending message", e);
				}
			});
//			_sessionTracker.logMessageSent(this, msg);
		} catch (JsonProcessingException e) {
			log.warn("error broadcasting a all users", e);
		}
	}

	//RWorker.Delegate
	@Override
	public void broadcastToSingleClient(BaseResponse response, int socketId) {
		try {
			Object serializedResponse;
			if (response.isBinaryMessage())
				serializedResponse = _msgPackMapper.writeValueAsBytes(response);
			else
				serializedResponse = _mapper.writeValueAsString(response);
			RCSessionSocket socket = _webSockets.stream().filter(p -> p.getSocketId() == socketId).findFirst().get();
			socket.sendResponse(serializedResponse);
		} catch (Exception e) {
			log.warn("error sending single message", e);
		}
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
