package edu.wvu.stat.rc2.rworker.message;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VariableValueMessage extends BaseMessage {
	private final long _startTime;
	private final Map<String,Object> _value;
	
	@JsonCreator
	public VariableValueMessage(
			@JsonProperty("msg") String msg,
			@JsonProperty("startTime") long startTime,
			@JsonProperty("value") Map<String,Object> value)
	{
		super(msg);
		_startTime = startTime;
		_value = value;
	}
	
	public long getStartTime() { return _startTime; }
	public Map<String,Object> getValue() { return _value; }
	
}
