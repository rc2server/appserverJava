package edu.wvu.stat.rc2.rworker.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import edu.wvu.stat.rc2.rworker.ServerMessageResolver;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property="msg")
@JsonTypeIdResolver(ServerMessageResolver.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BaseRResponse {
	protected final String _msg;
	
	public BaseRResponse(@JsonProperty("msg") String msg) {
		_msg = msg;
		
	}
	
	public String getMsg() { return _msg; }
}
