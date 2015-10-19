package edu.wvu.stat.rc2.ws.request;

import com.fasterxml.jackson.annotation.JsonCreator;

public class UserListRequest extends BaseRequest {
	@JsonCreator
	public UserListRequest() {
		super("userList");
	}
}
