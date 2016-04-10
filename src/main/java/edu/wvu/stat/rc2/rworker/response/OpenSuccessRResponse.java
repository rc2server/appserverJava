package edu.wvu.stat.rc2.rworker.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenSuccessRResponse extends BaseRResponse {
	private boolean _success;
	private String _errorMessage;
	
	public OpenSuccessRResponse(
			@JsonProperty("success") boolean success,
			@JsonProperty("errorMessage") String errMsg
			) 
	{
		super("opensuccess");
		_success = success;
		_errorMessage = errMsg;
	}
	
	public boolean getSuccess() { return _success; }
	public String getErrorMessage() { return _errorMessage; }
}
