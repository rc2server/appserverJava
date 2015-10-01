package edu.wvu.stat.rc2.persistence;

import java.util.ArrayList;

import javax.sql.DataSource;

import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.api.jdbc.PGNotificationListener;
import com.impossibl.postgres.jdbc.PGDataSource;

import edu.wvu.stat.rc2.jdbi.BigIntegerArgumentFactory;

public class Rc2DataSourceFactory {
	final static Logger log = LoggerFactory.getLogger("rc2.Rc2DataSourceFactory");

	private final DataSource _ds;
	private final ArrayList<ListenerProxy> _listeners;
	private PGConnection _listenerConnection;
	private final String _host;
	private final String _userid;
	private final String _dbname;
	
	/** defaults to using test database on localhost */
	public Rc2DataSourceFactory() {
		this(System.getProperty("rc2.dbhost", "localhost"), "rc2", System.getProperty("rc2.dbname", "rc2test"));
	}
	
	public Rc2DataSourceFactory(String host, String user, String database) {
		_host = host;
		_userid = user;
		_dbname = database;
		PGDataSource pgds=null;
		log.info("connecting to " + _host + "/" + _dbname);
		pgds = new PGDataSource();
		pgds.setUser(_userid);
		pgds.setDatabase(_dbname);
		pgds.setHost(_host);
		_ds = pgds;
		if (_ds instanceof PGDataSource)
			pgds = (PGDataSource)_ds;
		pgds.setApplicationName("rc2 REST server");
		_listeners = new ArrayList<ListenerProxy>();
	}
	
	public DBI createDBI() {
		DBI dbi = new DBI(_ds);
		dbi.registerArgumentFactory(new BigIntegerArgumentFactory());
		return dbi;
	}
	
	public Rc2DAO createDAO() {
		log.debug("creating DAO");
		DBI dbi = createDBI();
		return new Rc2DAO(dbi, getDBHost(), getDBUser(), getDBDatabase());
	}
	
	public String getDBHost() { return _host; }
	public String getDBUser() { return _userid; }
	public String getDBDatabase() { return _dbname; }
	
	private boolean listenerConnectionValid()  {
		if (null == this._listenerConnection)
			return false;
		try {
			return this._listenerConnection.isValid(1);
		} catch (Exception e) {
			//this shouldn't throw a typed exception. only throws if illegal argument, should be runtime excepiton
		}
		return false;
	}
	
	/**
		@param channelPattern a regex pattern for what channels to listen for
		@param listener the listener to be notified. Can use a lambda.
	 */
	public void addNotificationListener(String channelPattern, NotificationListener listener) {
		if (!listenerConnectionValid()) {
			try {
				_listenerConnection = (PGConnection) _ds.getConnection();
			} catch (Exception e) {
				log.error("failed to get a listener connection for database", e);
				throw new RuntimeException("failed to add listener");
			}
		}
		ListenerProxy proxy = new ListenerProxy(listener, channelPattern);
		_listenerConnection.addNotificationListener(channelPattern, proxy);
		_listeners.add(proxy);
		log.debug("added notification listener for: " + channelPattern);
	}
	
	/** removes the specified listener 
	 	@param listener the listener to remove
	 */
	public void removeNotificationListener(NotificationListener listener) {
		_listeners.stream()
			.filter(x -> x._listener == listener).findFirst()
			.ifPresent(proxy -> {
				_listenerConnection.removeNotificationListener(proxy);
				_listeners.remove(proxy);
				log.debug("removed notification listener for: " + proxy._pattern);
			});
	}
	
	/** Handles notifications from the database */
	public interface NotificationListener {
		public void handleNotification(String channelName, String message);
	}
	
	/** used to hide the underlying PG implementation */
	class ListenerProxy implements PGNotificationListener {
		NotificationListener _listener;
		final String _pattern;
		
		ListenerProxy(NotificationListener listener, String pattern) {
			_listener = listener;
			_pattern = pattern;
		}
		
		@Override
		public void notification(int processId, String channelName, String payload) {
			log.debug("received db notification on channel '" + channelName + "' with payload:" + payload);
			_listener.handleNotification(channelName, payload);
		}
		
	}
}

