package edu.wvu.stat.rc2.ws;

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
import edu.wvu.stat.rc2.persistence.RCSessionRecord;
import edu.wvu.stat.rc2.persistence.RCWorkspace;
import edu.wvu.stat.rc2.persistence.RCWorkspaceQueries;
import edu.wvu.stat.rc2.persistence.Rc2DAO;

@SuppressWarnings("unused")
public final class RCSession implements RCSessionSocket.Delegate {
	static final Logger log = LoggerFactory.getLogger("rc2.RCSession");

	private final Rc2DataSourceFactory _dbfactory;
	private RCWorkspace _wspace;
	private final List<RCSessionSocket> _webSockets;
	private ObjectMapper _mapper;
	private Rc2DAO _dao;
	
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
		
		RCSessionRecord.Queries srecDao = _dao.getDBI().onDemand(RCSessionRecord.Queries.class);
		_sessionId = srecDao.createSessionRecord(_wspace.getId());
	}

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
	
	private void handleExecuteScript(Map<String,Object> cmdObj, RCSessionSocket socket) {
		
	}

	private void handleExecuteScriptFile(Map<String,Object> cmdObj, RCSessionSocket socket) {
		
	}

	private void handleSessionList(Map<String,Object> cmdObj, RCSessionSocket socket) {
		
	}

	private void handleWatchVariables(Map<String,Object> cmdObj, RCSessionSocket socket) {
		
	}

	private void handleGetVariable(Map<String,Object> cmdObj, RCSessionSocket socket) {
		
	}


	public synchronized void broadcastToAllClients(Map<String,Object> msg) {
		try {
			String msgStr = _mapper.writeValueAsString(msg);
//			if (null != _jsonLog)
//				_jsonLog.println("to all:" + msgStr);
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
	
	
	@Override
	public void websocketUseDatabaseHandle(HandleCallback<Void> callback) {
		_dao.getDBI().withHandle(callback);
	}

	@Override
	public Map<String, Object> getSessionDescriptionForWebsocket(RCSessionSocket socket) {
		return null;
	}

	@Override
	public void websocketOpened(RCSessionSocket socket) {
		_webSockets.add(socket);
	}

	@Override
	public void websocketClosed(RCSessionSocket socket) {
		_webSockets.remove(socket);
	}

	@Override
	public void processWebsocketMessage(RCSessionSocket socket, Map<String, Object> msg) {
		String cmdStr = null;
		try {
			cmdStr = _mapper.writeValueAsString(msg);
//			if (null != _jsonLog)
//				_jsonLog.println(socket.getSocketId() + " said:" + cmdStr);
		} catch (Exception e) {
		}
//		_sessionTracker.logMessageReceived(this, socket, cmdObj);
		log.info("rcvd command:" + cmdStr);
		Map<String,Object> resObj = new HashMap<String,Object>();
		try {
			String cmd = (String)msg.get("cmd");
			Dispatcher d = Dispatcher.valueOf(cmd);
			d.handleCommand(this, msg, resObj, socket);
		} catch (Exception e) {
			log.warn("exception evaluating command", e);
			try {
				resObj.put(MsgKey_status, "unknown error");
			} catch (Exception ee) {
				ee.printStackTrace();
			}
			broadcastToAllClients(resObj);
		}
	}

	@Override
	public void processWebsocketBinaryMessage(RCSessionSocket socket, byte[] data, int offset, int length) {
	}
	
	enum Dispatcher {
		executeScript {
			@Override
			void handleCommand(RCSession session, Map<String,Object> cmdObj, Map<String,Object> response, 
					RCSessionSocket socket)  
			{
				session.handleExecuteScript(cmdObj, socket);
			}
		},
		executeScriptFile {
			@Override
			void handleCommand(RCSession  session, Map<String,Object> cmdObj, Map<String,Object> response, 
					RCSessionSocket socket) 
			{
				session.handleExecuteScriptFile(cmdObj, socket);
			}
		},
		userlist {
			@Override
			void handleCommand(RCSession  session, Map<String,Object> cmdObj, Map<String,Object> response, 
					RCSessionSocket socket) 
			{
				session.handleSessionList(cmdObj, socket);
			}
		},
		keepAlive {
			@Override
			void handleCommand(RCSession  session, Map<String,Object> cmdObj, Map<String,Object> response, 
					RCSessionSocket socket)  
			{
			}
		},
		watchvariables {
			@Override
			void handleCommand(RCSession  session, Map<String,Object> cmdObj, Map<String,Object> response, 
					RCSessionSocket socket) 
			{
				session.handleWatchVariables(cmdObj, socket);
			}
		},
		getVariable {
			@Override
			void handleCommand(RCSession  session, Map<String,Object> cmdObj, Map<String,Object> response, 
					RCSessionSocket socket) 
			{
				session.handleGetVariable(cmdObj, socket);
			}
		},
		help {
			@Override
			void handleCommand(RCSession  session, Map<String,Object> cmdObj, Map<String,Object> response, 
					RCSessionSocket socket) 
			{
				session.handleGetVariable(cmdObj, socket);
			}
		};
		abstract void handleCommand(RCSession  session, Map<String,Object> cmdObj, Map<String,Object> response, 
				RCSessionSocket socket) ;
	};

	enum MessageKeys {
		msg, status, user, fname, sid
	};
	
	private static final String MsgKey_msg = "msg";
	private static final String MsgKey_status = "status";
	private static final String MsgKey_user = "user";
	private static final String MsgKey_fname = "fname";
	private static final String MsgKey_sid = "sid";

}
