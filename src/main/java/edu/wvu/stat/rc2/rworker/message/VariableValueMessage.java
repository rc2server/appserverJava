package edu.wvu.stat.rc2.rworker.message;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VariableValueMessage extends BaseMessage {
	private final long _startTime;
	private final Map<String,Object> _value;
	private final long _userIdentifier;
	
	@JsonCreator
	public VariableValueMessage(
			@JsonProperty("msg") String msg,
			@JsonProperty("startTime") long startTime,
			@JsonProperty("userIdentifier") long userIdent,
			@JsonProperty("value") Map<String,Object> value)
	{
		super(msg);
		_startTime = startTime;
		_value = value;
		_userIdentifier = userIdent;
	}
	
	public long getStartTime() { return _startTime; }
	public long getUserIdentifier() { return _userIdentifier; }
	public Map<String,Object> getValue() { return _value; }
	
}
