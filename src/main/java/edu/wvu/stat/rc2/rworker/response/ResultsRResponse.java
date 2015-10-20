package edu.wvu.stat.rc2.rworker.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultsRResponse extends BaseRResponse {
	private final String _string;
	
	@JsonCreator
	public ResultsRResponse(
			@JsonProperty("msg") String msg,
			@JsonProperty("string") String string
		)
	{
		super(msg);
		_string = string;
	}
	
	public String getString() { return _string; }
}
