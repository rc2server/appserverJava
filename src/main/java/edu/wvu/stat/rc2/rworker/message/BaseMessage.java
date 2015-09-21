package edu.wvu.stat.rc2.rworker.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import edu.wvu.stat.rc2.rworker.ServerMessageResolver;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property="msg")
@JsonTypeIdResolver(ServerMessageResolver.class)
public abstract class BaseMessage {
	protected final String _msg;
	
	public BaseMessage(@JsonProperty("msg") String msg) {
		_msg = msg;
		
	}
	
	public String getMsg() { return _msg; }
}
