package edu.wvu.stat.rc2.ws.resposne;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse extends BaseResponse {
	private final String _error;
	
	public ErrorResponse(String msg) {
		super("error");
		_error = msg;
	}
	
	@JsonProperty
	public String getError() { return _error; }
}
