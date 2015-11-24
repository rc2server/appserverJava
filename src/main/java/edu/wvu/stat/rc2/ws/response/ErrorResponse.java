package edu.wvu.stat.rc2.ws.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse extends BaseResponse {
	private final String _error;
	
	public ErrorResponse(String msg) {
		this(msg, 0);
	}
	
	public ErrorResponse(String msg, int queryId) {
		super("error", queryId);
		_error = msg;
	}
	
	@JsonProperty
	public String getError() { return _error; }
}
