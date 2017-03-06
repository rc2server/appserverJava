package edu.wvu.stat.rc2.rworker.response;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VariableUpdateRResponse extends BaseRResponse {
	private final Map<String,Object> _variables;
	private final int _userIdentifier;
	private final boolean _isDelta;
	
	@JsonCreator
	public VariableUpdateRResponse(
			@JsonProperty("msg") String msg,
			@JsonProperty("userIdentifier") int userIdent,
			@JsonProperty("variables") Map<String,Object> vars,
			@JsonProperty("delta") boolean delta
			) 
	{
		super(msg);
		//if vars.assigned is null, add an empty array
		Map<String, Object> varMap = vars;
		if (delta && vars.get("assigned") == null) {
			HashMap<String,Object> hmap = new HashMap<String, Object>(varMap);
			hmap.put("assigned", new HashMap<String,Object>());
			varMap = hmap;
		}
		_variables = varMap;
		_isDelta = delta;
		_userIdentifier = userIdent;
	}
	
	public int getUserIdentifier() { return _userIdentifier; }
	public Map<String,Object> getVariables() { return _variables; }
	public boolean isDelta() { return _isDelta; }
}
