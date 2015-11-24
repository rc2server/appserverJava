package edu.wvu.stat.rc2.ws.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultsResponse extends BaseResponse {
	private final String _string;
	private final int _fileId;
	
	public ResultsResponse(String string, int fileId, int queryId) {
		super("results", queryId);
		_string = string;
		_fileId = fileId;
	}
	
	@JsonProperty
	public String getString() { return _string; }
	@JsonProperty
	public int getFileId() { return _fileId; }
}
