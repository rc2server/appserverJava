package edu.wvu.stat.rc2.rworker.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ShowOutputMessage extends BaseMessage {
	private final int _fileId;
	
	@JsonCreator
	public ShowOutputMessage(@JsonProperty("msg") String msg, @JsonProperty("fileId") int fileId) {
		super(msg);
		_fileId = fileId;
	}
	
	public int getFileId() { return _fileId; }
}
