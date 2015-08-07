package edu.wvu.stat.rc2.resources;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wvu.stat.rc2.RCCustomError;
import edu.wvu.stat.rc2.RCError;
import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.rs.RCUserPrincipal;
import edu.wvu.stat.rc2.rs.Rc2DBInject;

public abstract class BaseResource {
	final static Logger log= LoggerFactory.getLogger(BaseResource.class);
	
	@Context SecurityContext _securityContext;
	@Rc2DBInject DBI _dbi;
	private RCUser _testUser;

	public BaseResource() {}

	/** constructor for unit tests to bypass injection */
	BaseResource(DBI dbi, RCUser user) {
		_dbi = dbi;
		_testUser = user;
	}

	protected RCUser getUser() {
		Principal p = _securityContext.getUserPrincipal();
		if (p instanceof RCUserPrincipal)
			return ((RCUserPrincipal)p).getUser();
		log.warn("getUser() called on BaseResource subclass while not logged in");
		return _testUser;
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
