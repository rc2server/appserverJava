package edu.wvu.stat.rc2.rworker.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExecCompleteMessage extends BaseMessage {
	private final long _startTime;
	private final int _imgBatchId;
	private final List<Integer> _imageIds;
	private final List<String> _filesModified;
	private final List<String> _filesDeleted;
	
	@JsonCreator
	public ExecCompleteMessage(
			@JsonProperty("msg") String msg, 
			@JsonProperty("startTime") String startTime,
			@JsonProperty("filesModified") List<String> modFiles,
			@JsonProperty("filesDeleted") List<String> delFiles,
			@JsonProperty("images") List<Integer> imageIds,
			@JsonProperty("imgBatch") int imgBatchId
			) 
	{
		super(msg);
		if (null == startTime)
			startTime = "0";
		_startTime = Long.parseLong(startTime);
		_imageIds = imageIds;
		_filesModified = modFiles;
		_filesDeleted = delFiles;
		_imgBatchId = imgBatchId;
	}
	
	public long getStartTime() { return _startTime; }
	public List<Integer> getImageIds() { return _imageIds; }
	public List<String> getFilesModified() { return _filesModified; }
	public List<String> getFilesDeleted() { return _filesDeleted; }
	public int getImageBatchId() { return _imgBatchId; }
}

