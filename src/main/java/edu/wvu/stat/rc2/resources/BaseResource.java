package edu.wvu.stat.rc2.resources;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wvu.stat.rc2.PermissionChecker;
import edu.wvu.stat.rc2.RCCustomError;
import edu.wvu.stat.rc2.RCError;
import edu.wvu.stat.rc2.persistence.RCLoginToken;
import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.persistence.Rc2DAO;
import edu.wvu.stat.rc2.rs.Rc2DBInject;
import static edu.wvu.stat.rc2.Rc2AppConfiguration.*;

public abstract class BaseResource {
	final static Logger log= LoggerFactory.getLogger("rc2.BaseResource");
	
	@Context  HttpServletRequest _servletRequest;
	@Rc2DBInject Rc2DAO _dao;
	private RCUser _user;
	private PermissionChecker _permChecker;

	public BaseResource() {}

	/** constructor for unit tests to bypass injection */
	BaseResource(Rc2DAO dao, RCUser user) {
		_dao = dao;
		_user = user;
	}

	protected PermissionChecker getPermChecker() {
		if (null == _permChecker)
			_permChecker = new PermissionChecker(getUser());
		return _permChecker;
	}
	
	protected RCUser getUser() {
		if (null == _user)
			_user = (RCUser)_servletRequest.getAttribute(UserSessionKey);
		return _user;
	}
	
	protected RCLoginToken getLoginToken() {
		return (RCLoginToken) _servletRequest.getAttribute(LoginTokenKey);
	}
	
	public void throwRestError(RCRestError error) throws WebApplicationException {
		Response rsp = Response.status(error.getHttpCode())
							.entity(Arrays.asList(error))
							.build();
		throw new WebApplicationException(rsp);
		
	}

	public void throwCustomRestError(RCRestError error, String details) throws WebApplicationException {
		RCCustomError cerr = new RCCustomError(error, details);
		Response rsp = Response.status(error.getHttpCode())
							.entity(Arrays.asList(cerr))
							.build();
		throw new WebApplicationException(rsp);
		
	}

	public List<RCError> formatErrorResponse(RCRestError error) {
		return Arrays.asList(error);
	}

	public List<RCError> formatErrorResponse(List<RCError> errors) {
		return errors;
	}
	
	public Map<String,Object> formatSingleResponse(String name, Object obj) {
		Map<String,Object> map = new HashMap<>();
		map.put("status", 0);
		map.put(name, obj);
		return map;
	}
}
