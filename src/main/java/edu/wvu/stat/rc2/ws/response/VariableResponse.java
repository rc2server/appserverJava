package edu.wvu.stat.rc2.ws.response;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VariableResponse extends BaseResponse {
	private final int _socketId;
	private final Map<String,Object> _variables;
	private final boolean _singleValue;
	private final boolean _delta;
	
	public VariableResponse(Map<String,Object> variables, int socketId, boolean delta, boolean singleValue) {
		super("variables", 0);
		_socketId = socketId;
		_variables = ImmutableMap.copyOf(variables);
		_singleValue = singleValue;
		_delta = delta;
	}
	
	@JsonIgnore
	public int getSocketId() { return _socketId; }
	
	@JsonProperty
	public Map<String,Object> getVariables() { return _variables; }
	@JsonProperty
	public boolean isSingleValue() { return _singleValue; }
	@JsonProperty 
	public boolean isDelta() { return _delta; }
	
}
