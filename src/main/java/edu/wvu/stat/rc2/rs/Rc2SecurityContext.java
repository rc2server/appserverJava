package edu.wvu.stat.rc2.rs;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import edu.wvu.stat.rc2.persistence.RCLoginToken;
import edu.wvu.stat.rc2.persistence.RCUser;

public class Rc2SecurityContext implements SecurityContext {
	RCUserPrincipal _principal;
	RCLoginToken _token;
	
	public Rc2SecurityContext(RCUser user, RCLoginToken token) {
		_principal = new RCUserPrincipal(user);
		_token = token;
	}
	
	@Override
	public Principal getUserPrincipal() {
		return _principal;
	}

	@Override
	public boolean isUserInRole(String role) {
		return false;
	}

	@Override
	public boolean isSecure() {
		return true;
	}

	@Override
	public String getAuthenticationScheme() {
		return "rc2";
	}
	
	public RCLoginToken getToken() {
		return _token;
	}
	
}
