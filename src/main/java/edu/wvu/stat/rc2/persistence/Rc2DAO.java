package edu.wvu.stat.rc2.persistence;

import org.skife.jdbi.v2.DBI;

public class Rc2DAO {
	private final DBI _dbi;
	private volatile RCUser.Queries _userDao;
	
	Rc2DAO(DBI dbi) {
		_dbi = dbi;
	}
	
	public RCUser findUserById(int userId) {
		return getUserDao().findById(userId);
	}
	
	//uses double check idom for fast performance (likely not needed). Would be simpler to just make synchronized
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
