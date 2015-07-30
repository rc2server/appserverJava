package edu.wvu.stat.rc2.resources;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.rs.UserInject;

public abstract class BaseResource {
	
	@UserInject
	RCUser _user;
	
	public Map<String,Object> formatErrorResponse(RCRestError error) {
		return formatErrorResponse(Arrays.asList(error));
	}

	public Map<String,Object> formatErrorResponse(List<RCRestError> errors) {
		HashMap<String,Object> map = new HashMap<>();
		map.put("status", 1);
		map.put("errors", errors);
		return map;
	}
}
