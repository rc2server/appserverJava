package edu.wvu.stat.rc2.rworker.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ShowOutputRResponse extends BaseRResponse {
	private final int _fileId;
	
	@JsonCreator
	public ShowOutputRResponse(@JsonProperty("msg") String msg, @JsonProperty("fileId") int fileId) {
		super(msg);
		_fileId = fileId;
	}
	
	public int getFileId() { return _fileId; }
}
