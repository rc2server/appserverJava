package edu.wvu.stat.rc2.ws.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BaseResponse {
	protected final String _msg;
	
	@JsonCreator
	public BaseResponse(@JsonProperty("msg") String msg) {
		_msg = msg;
		
	}
	
	@JsonProperty
	public String getMsg() { return _msg; }

}
