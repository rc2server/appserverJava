package edu.wvu.stat.rc2.ws.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EchoQueryResponse extends BaseResponse {
	private final String _query;
	private final int _fileId;
	
	public EchoQueryResponse(int queryId, int fileId, String query) {
		super("echo", queryId);
		_query = query;
		_fileId = fileId;
	}
	
	@JsonProperty
	public String getQuery() { return _query; }
	
	@JsonProperty
	public int getFileId() { return _fileId; }
}
