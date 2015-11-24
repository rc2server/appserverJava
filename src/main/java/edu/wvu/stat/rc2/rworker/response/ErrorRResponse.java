package edu.wvu.stat.rc2.rworker.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ErrorRResponse extends BaseRResponse {
	private final String _details;
	private final int _code;
	private final int _queryId;
	
	public ErrorRResponse(@JsonProperty("msg") String msg, 
			@JsonProperty("errorDetails") String details, 
			@JsonProperty("errorCode") int code,
			@JsonProperty("queryId") int queryId
			) 
	{
		super(msg);
		_details = details;
		_code = code;
		_queryId = queryId;
	}
	
	public int getCode() { return _code; }
	public String getDetails() { return _details; }
	public int getQueryId() { return _queryId; }
}
