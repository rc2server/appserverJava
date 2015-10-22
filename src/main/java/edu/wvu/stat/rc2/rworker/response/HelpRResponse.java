package edu.wvu.stat.rc2.rworker.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HelpRResponse extends BaseRResponse {
	private final String _topic;
	private List<String> _paths;
	
	@JsonCreator
	public HelpRResponse(
			@JsonProperty("msg") String msg,
			@JsonProperty("topic") String topic,
			@JsonProperty("paths") List<String> paths
		)
	{
		super(msg);
		_topic = topic;
		_paths = ImmutableList.copyOf(paths);
	}
	
	public String getTopic() { return _topic; }
	public List<String> getPaths() { return _paths; }
}
