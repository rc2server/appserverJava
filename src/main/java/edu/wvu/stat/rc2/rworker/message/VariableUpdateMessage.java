package edu.wvu.stat.rc2.rworker.message;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VariableUpdateMessage extends BaseMessage {
	private final Map<String,Object> _variables;
	private final boolean _isDelta;
	
	@JsonCreator
	public VariableUpdateMessage(
			@JsonProperty("msg") String msg,
			@JsonProperty("variables") Map<String,Object> vars,
			@JsonProperty("delta") boolean delta
			) 
	{
		super(msg);
		_variables = vars;
		_isDelta = delta;
	}
	
	public Map<String,Object> getVariables() { return _variables; }
	public boolean isDelta() { return _isDelta; }
}
