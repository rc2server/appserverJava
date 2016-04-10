package edu.wvu.stat.rc2.ws.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.wvu.stat.rc2.persistence.RCFile;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultsResponse extends BaseResponse {
	private final String _string;
	private final RCFile _file;

	public ResultsResponse(String string, RCFile file, int queryId) {
		super("results", queryId);
		_string = string;
		_file = file;
	}
	
	@JsonProperty
	public String getString() { return _string; }
	@JsonProperty
	public RCFile getFile() { return _file; }
}
