package edu.wvu.stat.rc2.ws;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wvu.stat.rc2.persistence.PGDataSourceFactory;
import io.dropwizard.lifecycle.Managed;

public class RCSessionCache implements Managed {
	private final ConcurrentHashMap<Number, RCSession> _sessionMap;
	private final PGDataSourceFactory _dbfactory;
	private final ObjectMapper _mapper;
	private CleanupTask _cleanupTask;
	
	public RCSessionCache(PGDataSourceFactory dbfactory, ObjectMapper mapper) {
		_sessionMap = new ConcurrentHashMap<Number, RCSession>();
		_dbfactory = dbfactory;
		_mapper = mapper;
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

	public RCSession sessionForWorkspace(int wspaceId, int userId) {
		RCSession session=null;
		synchronized (_sessionMap) {
			session = _sessionMap.get(wspaceId);
			if (null == session) {
				session = new RCSession(_dbfactory, _mapper, wspaceId);
				_sessionMap.put(wspaceId, session);
			}
		}
		return session;
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
