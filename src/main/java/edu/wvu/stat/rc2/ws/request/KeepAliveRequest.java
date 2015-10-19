package edu.wvu.stat.rc2.ws.request;

import com.fasterxml.jackson.annotation.JsonCreator;

public class KeepAliveRequest extends BaseRequest {
	@JsonCreator
	public KeepAliveRequest() {
		super("keepAlive");
	}
}
