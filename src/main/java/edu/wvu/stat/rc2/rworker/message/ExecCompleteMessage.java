package edu.wvu.stat.rc2.rworker.message;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExecCompleteMessage extends BaseMessage {
	private final long _startTime;
	private final Map<String,Object> _clientData;
	private final List<Integer> _imageIds;
	private final List<String> _filesModified;
	private final List<String> _filesDeleted;
	
	@JsonCreator
	public ExecCompleteMessage(
			@JsonProperty("msg") String msg, 
			@JsonProperty("startTime") long startTime,
			@JsonProperty("clientData") Map<String,Object> clientData,
			@JsonProperty("filesModified") List<String> modFiles,
			@JsonProperty("filesDeleted") List<String> delFiles,
			@JsonProperty("images") List<Integer> imageIds
			) 
	{
		super(msg);
		_startTime = startTime;
		_clientData = clientData;
		_imageIds = imageIds;
		_filesModified = modFiles;
		_filesDeleted = delFiles;
	}
	
	public long getStartTime() { return _startTime; }
	public Map<String,Object> getClientData() { return _clientData; }
	public List<Integer> getImageIds() { return _imageIds; }
	public List<String> getFilesModified() { return _filesModified; }
	public List<String> getFilesDeleted() { return _filesDeleted; }
}

