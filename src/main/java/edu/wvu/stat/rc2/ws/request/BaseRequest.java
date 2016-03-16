package edu.wvu.stat.rc2.ws.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import edu.wvu.stat.rc2.ws.ClientMessageResolver;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.EXISTING_PROPERTY, property="msg", visible=false)
@JsonTypeIdResolver(ClientMessageResolver.class)
public class BaseRequest {
	protected final String _msg;
	
	@JsonCreator
	public BaseRequest(@JsonProperty("msg") String msg) {
		_msg = msg;
	}
	
	@JsonProperty
	public String getMsg() { return _msg; }

}
