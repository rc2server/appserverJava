package edu.wvu.stat.rc2.ws.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecuteRequest extends BaseRequest {
	private final String _code;
	private final String _type;
	private final int _fileId;
	private final boolean _noEcho;
	
	@JsonCreator
	public ExecuteRequest(@JsonProperty("code") String code,
			@JsonProperty("fileId") int fileId,
			@JsonProperty("type") String type,
			@JsonProperty("noEcho") boolean noEcho) 
	{
		super("execute");
		_code = code;
		_fileId = fileId;
		_type = type;
		_noEcho = noEcho;
	}
	
	@JsonProperty
	public String getCode() { return _code; }
	
	@JsonProperty
	public int getFileId() { return _fileId; }
	
	@JsonProperty
	public String getType() { return _type; }
	
	@JsonProperty
	public boolean getNoEcho() { return _noEcho; }
	
	@Override
	public String toString() {
		return "executeRequest:file=" + _fileId + ", type=" + _type;
	}
}
