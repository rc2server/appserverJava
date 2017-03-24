package edu.wvu.stat.rc2.persistence;

import java.sql.Statement;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.IntegerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.api.jdbc.PGNotificationListener;
import com.impossibl.postgres.jdbc.PGDataSource;

import edu.wvu.stat.rc2.config.DatabaseConfig;
import edu.wvu.stat.rc2.jdbi.BigIntegerArgumentFactory;
//import io.dropwizard.jdbi.logging.LogbackLog;

public class Rc2DataSourceFactory {
	final static Logger log = LoggerFactory.getLogger("rc2.Rc2DataSourceFactory");
	final static int currentDBVersion = 2;

	private final DataSource _ds;
	private final ArrayList<ListenerProxy> _listeners;
	private PGConnection _listenerConnection;
	private final DatabaseConfig _dbConfig;
	private final String _host;
	private final String _userid;
	private final String _dbname;
	
	public Rc2DataSourceFactory(DatabaseConfig config) {
		_dbConfig = config;
		_host = _dbConfig.getDbhost();
		_userid = _dbConfig.getDbuser();
		_dbname = _dbConfig.getDbname();
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
		try {
			DBI dbi = createDBI();
			Handle h = dbi.open();
			int version = h.createQuery("select valueint from metadata where key = 'sqlSchemaVersion'")
					.map(IntegerMapper.FIRST)
					.first();
			if (version < currentDBVersion) {
				h.execute("alter table sessionimage add column title varchar(255)");
				h.execute("update metadata set valueint = ? where key = 'sqlSchemaVersion'", currentDBVersion);
				log.info("schema updated to version 2");
			}
		} catch (Exception e) {
			log.warn("error updating schema version", e);
		}
	}
	
	public DBI createDBI() {
		DBI dbi = new DBI(_ds);
		dbi.registerArgumentFactory(new BigIntegerArgumentFactory());
//		dbi.setSQLLog(new LogbackLog());
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
		@param channelName the channel to listen to. The name must be alphanumeric only
		@param listener the listener to be notified. Can use a lambda.
	 */
	public void addNotificationListener(String channelName, NotificationListener listener) {
		if (!listenerConnectionValid()) {
			try {
				_listenerConnection = (PGConnection) _ds.getConnection();
			} catch (Exception e) {
				log.error("failed to get a listener connection for database", e);
				throw new RuntimeException("failed to add listener");
			}
		}
		ListenerProxy proxy = new ListenerProxy(listener, channelName);
		_listenerConnection.addNotificationListener(channelName, proxy);
		try (Statement stmt = _listenerConnection.createStatement()) {
			stmt.executeUpdate("listen " + channelName);
		} catch (Exception e) {
			log.error("failed to setup dbnotification callback", e);
		}
		
		_listeners.add(proxy);
		log.debug("added notification listener for: " + channelName);
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

