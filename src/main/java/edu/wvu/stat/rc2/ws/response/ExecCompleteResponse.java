package edu.wvu.stat.rc2.ws.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.wvu.stat.rc2.persistence.RCSessionImage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecCompleteResponse extends BaseResponse {
	private final int _batchId;
	private List<RCSessionImage> _images;
	private boolean _expectShowOutput;
	
	public ExecCompleteResponse(int batchId, List<RCSessionImage> images, int queryId, boolean expectShowOutput) {
		super("results", queryId);
		_batchId = batchId;
		_images = images;
		_expectShowOutput = expectShowOutput;
	}
	
	@JsonProperty
	public int getImageBatchId() { return _batchId; }
	
	@JsonProperty
	public List<RCSessionImage> getImages() { return _images; }
	
	@JsonProperty
	public boolean getComplete() { return true; }
	
	@JsonProperty
	public boolean expectShowOutput() { return _expectShowOutput; }
}
