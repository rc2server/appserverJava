package edu.wvu.stat.rc2.rworker.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExecCompleteMessage extends BaseMessage {
	private final long _startTime;
	private final int _imgBatchId;
	private final List<Integer> _imageIds;
	
	@JsonCreator
	public ExecCompleteMessage(
			@JsonProperty("msg") String msg, 
			@JsonProperty("startTime") String startTime,
			@JsonProperty("images") List<Integer> imageIds,
			@JsonProperty("imgBatch") int imgBatchId
			) 
	{
		super(msg);
		if (null == startTime)
			startTime = "0";
		_startTime = Long.parseLong(startTime);
		_imageIds = imageIds;
		_imgBatchId = imgBatchId;
	}
	
	public long getStartTime() { return _startTime; }
	public List<Integer> getImageIds() { return _imageIds; }
	public int getImageBatchId() { return _imgBatchId; }
}

