package edu.wvu.stat.rc2.rs;

import java.security.Principal;

import edu.wvu.stat.rc2.persistence.RCUser;

public class RCUserPrincipal implements Principal {

	private final RCUser _user;
	
	public RCUserPrincipal(RCUser user) {
		_user = user;
	}
	
	public RCUser getUser() {
		return _user;
	}
	
	@Override
	public String getName() {
		return _user.getLogin();
	}

}
