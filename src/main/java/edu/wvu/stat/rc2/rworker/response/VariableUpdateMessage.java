package edu.wvu.stat.rc2.rworker.response;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VariableUpdateMessage extends BaseMessage {
	private final Map<String,Object> _variables;
	private final int _userIdentifier;
	private final boolean _isDelta;
	
	@JsonCreator
	public VariableUpdateMessage(
			@JsonProperty("msg") String msg,
			@JsonProperty("userIdentifier") int userIdent,
			@JsonProperty("variables") Map<String,Object> vars,
			@JsonProperty("delta") boolean delta
			) 
	{
		super(msg);
		_variables = vars;
		_isDelta = delta;
		_userIdentifier = userIdent;
	}
	
	public int getUserIdentifier() { return _userIdentifier; }
	public Map<String,Object> getVariables() { return _variables; }
	public boolean isDelta() { return _isDelta; }
}
