package edu.wvu.stat.rc2.ws.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GetVariableRequest extends BaseRequest {
	private final String _variable;
	
	@JsonCreator
	public GetVariableRequest(@JsonProperty("variable") String variable) {
		super("getVariable");
		_variable = variable;
	}
	
	@JsonProperty
	public String getVariable() { return _variable; }
}
