package edu.wvu.stat.rc2.persistence;

import org.skife.jdbi.v2.DBI;

public class Rc2DAO {
	private final DBI _dbi;
	private volatile RCUser.Queries _userDao;
	private volatile RCWorkspaceQueries _wsDao;
	
	Rc2DAO(DBI dbi) {
		_dbi = dbi;
	}
	
	public RCUser findUserById(int userId) {
		return getUserDao().findById(userId);
	}
	
	public RCWorkspace findWorkspaceById(int wsid) {
		return getWorkspaceDao().findById(wsid);
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
