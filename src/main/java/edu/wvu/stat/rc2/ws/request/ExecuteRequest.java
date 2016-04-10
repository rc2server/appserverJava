package edu.wvu.stat.rc2.ws.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExecuteRequest extends BaseRequest {
	private final String _code;
	private final String _type;
	private final int _fileId;
	
	@JsonCreator
	public ExecuteRequest(@JsonProperty("code") String code,
			@JsonProperty("fileId") int fileId,
			@JsonProperty("type") String type) 
	{
		super("execute");
		_code = code;
		_fileId = fileId;
		_type = type;
	}
	
	@JsonProperty
	public String getCode() { return _code; }
	
	@JsonProperty
	public int getFileId() { return _fileId; }
	
	@JsonProperty
	public String getType() { return _type; }
	
	@Override
	public String toString() {
		return "executeRequest:file=" + _fileId + ", type=" + _type;
	}
}
