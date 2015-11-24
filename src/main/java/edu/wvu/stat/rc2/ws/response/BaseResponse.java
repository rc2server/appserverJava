package edu.wvu.stat.rc2.ws.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseResponse {
	protected final String _msg;
	protected final int _queryId;
	
	@JsonCreator
	public BaseResponse(	@JsonProperty("msg") String msg,
							@JsonProperty("queryId") int queryId
						) 
	{
		_msg = msg;
		_queryId = queryId;
	}
	
	@JsonProperty
	public String getMsg() { return _msg; }
	
	@JsonProperty
	public int getQueryId() { return _queryId; }

}
