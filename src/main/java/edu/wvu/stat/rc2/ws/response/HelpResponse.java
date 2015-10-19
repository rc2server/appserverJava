package edu.wvu.stat.rc2.ws.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HelpResponse extends BaseResponse {
	private final String _topic;
	private final List<String> _paths;
	
	public HelpResponse(String topic, List<String> paths) {
		super("help");
		_topic = topic;
		_paths = paths;
	}
	
	@JsonProperty
	public String getTopic() { return _topic; }
	
	@JsonProperty
	public List<String> getPaths() { return _paths; }
}
