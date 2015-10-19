package edu.wvu.stat.rc2.ws;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wvu.stat.rc2.persistence.Rc2DataSourceFactory;
import edu.wvu.stat.rc2.rworker.RWorker;
import edu.wvu.stat.rc2.rworker.message.BaseMessage;
import edu.wvu.stat.rc2.ws.request.*;
import edu.wvu.stat.rc2.ws.resposne.BaseResponse;
import edu.wvu.stat.rc2.ws.resposne.ErrorResponse;
import edu.wvu.stat.rc2.persistence.RCSessionRecord;
import edu.wvu.stat.rc2.persistence.RCWorkspace;
import edu.wvu.stat.rc2.persistence.RCWorkspaceQueries;
import edu.wvu.stat.rc2.persistence.Rc2DAO;

//TODO: add shutdown hook for rworker
//		Runtime.getRuntime().addShutdownHook(new Thread(() -> _rworker.shutdown()));


@SuppressWarnings("unused")
public final class RCSession implements RCSessionSocket.Delegate, RWorker.Delegate {
	static final Logger log = LoggerFactory.getLogger("rc2.RCSession");

	private final Rc2DataSourceFactory _dbfactory;
	private RCWorkspace _wspace;
	private final List<RCSessionSocket> _webSockets;
	private ObjectMapper _mapper;
	private Rc2DAO _dao;
	private RWorker _rworker;
	
	private final long _startTime;
	private final int _sessionId;
	private boolean _watchingVariables;
	
	/**
	 @param dbfactory A factory is passed so that if the connection is dropped for some reason, a new one can be opened.
	 @param workspace The workspace this session represents.
	 @param mapper An object mapper to use for json conversion. If null, a generic mapper will be created.
	 */
	RCSession(Rc2DataSourceFactory dbfactory, ObjectMapper mapper, int wspaceId) {
		_dbfactory = dbfactory;
		_mapper = mapper;
		if (null == _mapper)
			_mapper = new ObjectMapper();
		_dao = _dbfactory.createDAO();
		_wspace = _dao.findWorkspaceById(wspaceId);
		if (null == _wspace)
			throw new IllegalArgumentException("invalid workspaceId");
		
		_webSockets = new ArrayList<RCSessionSocket>();
		_startTime = System.currentTimeMillis();
		
		_rworker = new RWorker(new RWorker.SocketFactory(), this);
		
		RCSessionRecord.Queries srecDao = _dao.getDBI().onDemand(RCSessionRecord.Queries.class);
		_sessionId = srecDao.createSessionRecord(_wspace.getId());
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
		RCSessionRecord.Queries srecDao = _dao.getDBI().onDemand(RCSessionRecord.Queries.class);
		srecDao.closeSessionRecord(_sessionId);
	}
	
	private void handleExecuteRequest(ExecuteRequest request) {
		
	}

	private void handleExecuteScriptFile(Map<String,Object> cmdObj, RCSessionSocket socket) {
		
	}

	private void handleSessionList(Map<String,Object> cmdObj, RCSessionSocket socket) {
		
	}

	private void handleWatchVariables(Map<String,Object> cmdObj, RCSessionSocket socket) {
		
	}

	private void handleGetVariable(Map<String,Object> cmdObj, RCSessionSocket socket) {
		
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
			Method m = getClass().getDeclaredMethod(methodName, req.getClass());
			m.invoke(this, req);
		} catch (Exception e) {
			log.error("error parsing client json", e);
			broadcastToAllClients(new ErrorResponse("unknown error"));
		}
	}

	//RCSessionSocket.Delegate
	@Override
	public void processWebsocketBinaryMessage(RCSessionSocket socket, byte[] data, int offset, int length) {
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
			String msgStr = _mapper.writeValueAsString(response);
			_webSockets.forEach(socket -> {
				try {
					socket.sendMessage(msgStr);
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
			RCSessionSocket socket = _webSockets.stream().filter(p -> p.getSocketId() == socketId).findFirst().get();
			String msgStr = _mapper.writeValueAsString(response);
			socket.sendMessage(msgStr);
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
