package edu.wvu.stat.rc2.rs;

import java.security.Principal;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.glassfish.hk2.api.Factory;

import edu.wvu.stat.rc2.persistence.RCUser;

public class LoggedInUserFactory implements Factory<RCUser> {
	private final HttpServletRequest _request;
	
	@Inject
	public LoggedInUserFactory(HttpServletRequest request) {
		_request = request;
	}
	
	@Override
	public void dispose(RCUser user) {
	}

	@Override
	public RCUser provide() {
		final Principal principal = _request.getUserPrincipal();
		return (RCUser)principal;
	}

}
