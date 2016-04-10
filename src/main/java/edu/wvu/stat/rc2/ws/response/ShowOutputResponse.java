package edu.wvu.stat.rc2.ws.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.wvu.stat.rc2.persistence.RCFile;

public class ShowOutputResponse extends BaseResponse {
	private final RCFile _file;

	public ShowOutputResponse(RCFile file, int queryId) {
		super("showOutput", queryId);
		_file = file;
	}
	
	@JsonProperty
	public RCFile getFile() { return _file; }

}
