package edu.wvu.stat.rc2.ws.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HelpRequest extends BaseRequest {
	private final String _topic;
	
	@JsonCreator
	public HelpRequest(@JsonProperty("topic") String topic) {
		super("help");
		_topic = topic;
	}
	
	@JsonProperty
	public String getTopic() { return _topic; }
}
