package edu.wvu.stat.rc2.ws.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExecuteRequest extends BaseRequest {
	private final String _code;
	private final int _fileId;
	
	@JsonCreator
	public ExecuteRequest(@JsonProperty("code") String code, @JsonProperty("fileId") int fileId) {
		super("execute");
		_code = code;
		_fileId = fileId;
	}
	
	@JsonProperty
	public String getCode() { return _code; }
	
	@JsonProperty
	public int getFileId() { return _fileId; }
}
