package edu.wvu.stat.rc2.rworker.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class ErrorMessage extends BaseMessage {
	private final String _details;
	private final int _code;
	
	public ErrorMessage(@JsonProperty("msg") String msg, 
			@JsonProperty("errorDetails") String details, 
			@JsonProperty("errorCode") int code) 
	{
		super(msg);
		_details = details;
		_code = code;
	}
	
	public int getCode() { return _code; }
	public String getDetails() { return _details; }
}
