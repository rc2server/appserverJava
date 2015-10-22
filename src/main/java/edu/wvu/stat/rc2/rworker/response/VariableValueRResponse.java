package edu.wvu.stat.rc2.rworker.response;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VariableValueRResponse extends BaseRResponse {
	private final Map<String,Object> _value;
	private final int _userIdentifier;
	
	@JsonCreator
	public VariableValueRResponse(
			@JsonProperty("msg") String msg,
			@JsonProperty("userIdentifier") int userIdent,
			@JsonProperty("value") Map<String,Object> value)
	{
		super(msg);
		_value = value;
		_userIdentifier = userIdent;
	}
	
	public int getUserIdentifier() { return _userIdentifier; }
	public Map<String,Object> getValue() { return _value; }
	
}
