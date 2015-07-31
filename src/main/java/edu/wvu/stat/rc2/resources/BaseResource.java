package edu.wvu.stat.rc2.resources;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.skife.jdbi.v2.DBI;

import edu.wvu.stat.rc2.RCError;
import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.rs.Rc2DBInject;
import edu.wvu.stat.rc2.rs.UserInject;

public abstract class BaseResource {
	
	@UserInject
	RCUser _user;
	@Rc2DBInject 
	DBI _dbi;

	public BaseResource() {}
	
	/** constructor for unit tests to bypass injection */
	BaseResource(DBI dbi, RCUser user) {
		_dbi = dbi;
		_user = user;
	}
	
	public void throwRestError(RCRestError error) throws WebApplicationException {
		Response rsp = Response.status(error.getHttpCode())
							.entity(Arrays.asList(error))
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
