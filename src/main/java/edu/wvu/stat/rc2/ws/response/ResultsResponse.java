package edu.wvu.stat.rc2.ws.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultsResponse extends BaseResponse {
	private final String _string;

	public ResultsResponse(String string, int queryId) {
		super("results", queryId);
		_string = string;
	}

	@JsonProperty
	public String getString() { return _string; }
}
