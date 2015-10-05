package edu.wvu.stat.rc2;

import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.persistence.RCWorkspace;

public class PermissionChecker {
	private RCUser _user;
	
	public PermissionChecker(RCUser user) {
		assert user != null;
		_user = user;
	}

	public RCUser getUser() { return _user; }

	public boolean canAccessWorkspace(RCWorkspace wspace) {
		assert wspace != null;
		return wspace.getId() > 0 && wspace.getUserId() == _user.getId();
	}
}
