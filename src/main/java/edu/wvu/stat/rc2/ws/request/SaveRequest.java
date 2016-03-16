package edu.wvu.stat.rc2.ws.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SaveRequest extends BaseRequest {
	private final int _apiVersion;
	private final String _transId;
	private final int _fileId;
	private final int _fileVersion;
	private final String _content;
	
	@JsonCreator
	public SaveRequest(@JsonProperty("apiVersion") int apiVersion,
			@JsonProperty("transId") String transId, 
			@JsonProperty("fileId") int fileId, 
			@JsonProperty("fileVersion") int fileVersion,
			@JsonProperty("content") String content)
	{
		super("save");
		_transId = transId;
		_fileId = fileId;
		_fileVersion = fileVersion;
		_content = content;
		_apiVersion = apiVersion;
	}

	@JsonProperty
	public String getTransId() { return _transId; }
	
	@JsonProperty
	public int getFileId() { return _fileId; }
	
	@JsonProperty
	public int getFileVersion() { return _fileVersion; }
	
	@JsonProperty
	public String getContent() { return _content; }
	
	@JsonProperty
	public int getApiVersion() { return _apiVersion; }
}
