package edu.wvu.stat.rc2.ws.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultsResponse extends BaseResponse {
	private final String _string;
	private final int _fileId;
	
	public ResultsResponse(String string, int fileId) {
		super("results");
		_string = string;
		_fileId = fileId;
	}
	
	@JsonProperty
	public String getString() { return _string; }
	@JsonProperty
	public int getFileId() { return _fileId; }
}
