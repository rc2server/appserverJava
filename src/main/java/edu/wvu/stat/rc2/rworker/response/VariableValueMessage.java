package edu.wvu.stat.rc2.rworker.response;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VariableValueMessage extends BaseMessage {
	private final Map<String,Object> _value;
	private final int _userIdentifier;
	
	@JsonCreator
	public VariableValueMessage(
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
