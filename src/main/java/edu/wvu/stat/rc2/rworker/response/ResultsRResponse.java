package edu.wvu.stat.rc2.rworker.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultsRResponse extends BaseRResponse {
	private final String _string;
	private final boolean _stderr;
	private final int _queryId;
	private final boolean _expectShowOutput;
	
	@JsonCreator
	public ResultsRResponse(
			@JsonProperty("msg") String msg,
			@JsonProperty("string") String string,
			@JsonProperty("stderr") boolean stderr,
			@JsonProperty("queryId") int queryId,
			@JsonProperty("expectShowOutput") boolean expectShowOutput
		)
	{
		super(msg);
		_string = string;
		_stderr = stderr;
		_queryId = queryId;
		_expectShowOutput = expectShowOutput;
	}
	
	public String getString() { return _string; }
	
	public boolean getStdError() { return _stderr; }
	public int getQueryId() { return _queryId; }
	public boolean getExpectShowOutput() { return _expectShowOutput; }
}
