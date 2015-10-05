package edu.wvu.stat.rc2.persistence;

import java.util.List;

import org.skife.jdbi.v2.DBI;

public class Rc2DAO {
	private final DBI _dbi;
	private final String _dbHost;
	private final String _dbUser;
	private final String _dbDatabase;
	private volatile RCUser.Queries _userDao;
	private volatile RCWorkspaceQueries _wsDao;
	private volatile RCSessionImage.Queries _imgDao;
	
	Rc2DAO(DBI dbi, String host, String user, String database) {
		_dbi = dbi;
		_dbHost = host;
		_dbUser = user;
		_dbDatabase = database;
	}
	
	public String getDBHost() { return _dbHost; }
	public String getDBUser() { return _dbUser; }
	public String getDBDatabase() { return _dbDatabase; }
	
	public RCUser findUserById(int userId) {
		return getUserDao().findById(userId);
	}
	
	public RCWorkspace findWorkspaceById(int wsid) {
		return getWorkspaceDao().findById(wsid);
	}
	
	public RCSessionImage findImageById(int batchId) {
		return getSessionImageDao().findById(batchId);
	}

	public List<RCSessionImage> findImageBatchById(int batchId) {
		return getSessionImageDao().findByBatchId(batchId);
	}
	
	//uses double check idiom for fast performance (25x over synchronized)
	public RCSessionImage.Queries getSessionImageDao() {
		RCSessionImage.Queries result = _imgDao;
		if (null == result) {
			synchronized(this) {
				result = _imgDao;
				if (result == null)
					_imgDao = result = _dbi.onDemand(RCSessionImage.Queries.class);
			}
		}
		return result;
	}
	
	//uses double check idiom for fast performance (25x over synchronized)
	public RCWorkspaceQueries getWorkspaceDao() {
		RCWorkspaceQueries result = _wsDao;
		if (null == result) {
			synchronized(this) {
				result = _wsDao;
				if (result == null)
					_wsDao = result = _dbi.onDemand(RCWorkspaceQueries.class);
			}
		}
		return result;
	}
	
	//uses double check idiom for fast performance (25x over synchronized)
	public RCUser.Queries getUserDao() {
		RCUser.Queries result = _userDao;
		if (null == result) {
			synchronized(this) {
				result = _userDao;
				if (result == null)
					_userDao = result = _dbi.onDemand(RCUser.Queries.class);
			}
		}
		return result;
	}
	
	public DBI getDBI() { return _dbi; }
}
