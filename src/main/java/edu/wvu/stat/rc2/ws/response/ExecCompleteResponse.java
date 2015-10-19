package edu.wvu.stat.rc2.ws.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.wvu.stat.rc2.persistence.RCSessionImage;

public class ExecCompleteResponse extends BaseResponse {
	private final int _batchId;
	private List<RCSessionImage> _images;
	
	public ExecCompleteResponse(int batchId, List<RCSessionImage> images) {
		super("results");
		_batchId = batchId;
		_images = images;
	}
	
	@JsonProperty
	public int getImageBatchId() { return _batchId; }
	
	@JsonProperty
	public List<RCSessionImage> getImages() { return _images; }
	
}
