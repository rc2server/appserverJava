package edu.wvu.stat.rc2.rworker.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShowOutputRResponse extends BaseRResponse {
	private final int _fileId;
	private final int _queryId;
	
	@JsonCreator
	public ShowOutputRResponse(	@JsonProperty("msg") String msg, 
								@JsonProperty("fileId") int fileId,
								@JsonProperty("queryId") int queryId
			) 
	{
		super(msg);
		_fileId = fileId;
		_queryId = queryId;
	}
	
	public int getFileId() { return _fileId; }
	public int getQueryId() { return _queryId; }
}
