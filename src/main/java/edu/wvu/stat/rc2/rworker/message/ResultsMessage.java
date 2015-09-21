package edu.wvu.stat.rc2.rworker.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultsMessage extends BaseMessage {
	private final String _string;
	
	@JsonCreator
	public ResultsMessage(
			@JsonProperty("msg") String msg,
			@JsonProperty("string") String string
		)
	{
		super(msg);
		_string = string;
	}
	
	public String getString() { return _string; }
}
