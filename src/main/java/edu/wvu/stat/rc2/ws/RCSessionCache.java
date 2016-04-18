package edu.wvu.stat.rc2.ws;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wvu.stat.rc2.persistence.Rc2DataSourceFactory;
import edu.wvu.stat.rc2.rworker.RWorker;
import edu.wvu.stat.rc2.config.SessionConfig;
import edu.wvu.stat.rc2.persistence.RCUser;
import io.dropwizard.lifecycle.Managed;

public class RCSessionCache implements Managed {
	private final ConcurrentHashMap<Number, RCSession> _sessionMap;
	private final Rc2DataSourceFactory _dbfactory;
	private final ObjectMapper _mapper;
	private final RWorkerFactory _workerFactory;
	private CleanupTask _cleanupTask;
	private final SessionConfig _config;
	
	public RCSessionCache(Rc2DataSourceFactory dbfactory, ObjectMapper mapper, SessionConfig config) {
		this(dbfactory, mapper, config, null);
	}
	
	public RCSessionCache(Rc2DataSourceFactory dbfactory, ObjectMapper mapper, SessionConfig config, RWorkerFactory wfactory) 
	{
		_sessionMap = new ConcurrentHashMap<Number, RCSession>();
		_dbfactory = dbfactory;
		_mapper = mapper;
		_config = config;
		if (null == wfactory)
			wfactory = new RWorkerFactory(new RWorker.SocketFactory());
		_workerFactory = wfactory;
	}
	
	public ObjectMapper getObjectMapper() { return _mapper; }
	
	@Override
	public void start() throws Exception {
		
	}
	
	@Override
	public void stop() throws Exception {
		closeSessions();
	}
	
	public synchronized void scheduleCleanupTask(ScheduledExecutorService execService) {
			if (null == _cleanupTask) {
				_cleanupTask = new CleanupTask();
				execService.scheduleAtFixedRate(_cleanupTask, 2, 2, TimeUnit.MINUTES);
			}
	}
	
	public synchronized void closeSessions() {
		synchronized (_sessionMap) {
			Enumeration<RCSession> en = _sessionMap.elements();
			while (en.hasMoreElements()) {
				RCSession rs = en.nextElement();
				rs.shutdown();
			}
			_sessionMap.clear();
		}
	}

	/**
		@param req servlet upgrade request to create the websocket with
		@param wspaceId id of the workspace of the session to use 
		@param user the user requesting to connect to the specified workspace
		@return a websocket connected to the requested session
	 */
	public RCSessionSocket socketForWorkspaceAndUser(ServletUpgradeRequest req, int wspaceId, RCUser user) 
	{
		RCSession session=null;
		synchronized (_sessionMap) {
			session = _sessionMap.get(wspaceId);
			if (null == session) {
				session = new RCSession(_dbfactory, _mapper, _config, wspaceId, _workerFactory.createWorker());
				_sessionMap.put(wspaceId, session);
			}
		}
		RCSessionSocket socket = new RCSessionSocket(req, session, getObjectMapper(), user);
		return socket;
	}
	
	/** factory to allow injection of rworker to create session with */
	public static class RWorkerFactory {
		private RWorker.SocketFactory _sfactory;
		public RWorkerFactory(RWorker.SocketFactory sfactory) {
			_sfactory = sfactory; 
		}
		
		public RWorker createWorker() {
			return new RWorker(_sfactory, null);
		}
	}
	
	private final class CleanupTask implements Runnable {
		CleanupTask() {}
		@Override
		public void run() {
			synchronized(_sessionMap) {
				ArrayList<RCSession> sessionsToFree = new ArrayList<RCSession>();
				Enumeration<RCSession> en = _sessionMap.elements();
				while (en.hasMoreElements()) {
					RCSession rs = en.nextElement();
					if (rs.getClientCount() < 1 && rs.isIdle()) {
						sessionsToFree.add(rs);
					}
				}
				//now iterate through the ones to remove
				Iterator<RCSession> itr = sessionsToFree.iterator();
				while (itr.hasNext()) {
					RCSession session = itr.next();
					session.shutdown();
					_sessionMap.remove(session.getWorkspaceId());
				}
			}
		}
	}

}
