package edu.wvu.stat.rc2.ws.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.wvu.stat.rc2.ws.SessionError;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse extends BaseResponse {
	private final SessionError _error;
	
	public ErrorResponse(SessionError err) {
		this(err, 0);
	}
	
	public ErrorResponse(SessionError err, int queryId) {
		super("error", queryId);
		_error = err;
	}
	
	@JsonProperty
	public SessionError getError() { return _error; }
}
