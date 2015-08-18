package edu.wvu.stat.rc2;

import org.skife.jdbi.v2.DBI;

import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.persistence.RCWorkspace;

public class PermissionChecker {
	@SuppressWarnings("unused") //likely to be used in the future
	private DBI _dbi;
	private RCUser _user;
	
	public PermissionChecker(DBI dbi, RCUser user) {
		assert user != null;
		assert dbi != null;
		_dbi = dbi;
		_user = user;
	}

	public RCUser getUser() { return _user; }

	public boolean canAccessWorkspace(RCWorkspace wspace) {
		assert wspace != null;
		int wspaceId = wspace.getId();
		return wspaceId > 0 && wspaceId == _user.getId();
	}
}
