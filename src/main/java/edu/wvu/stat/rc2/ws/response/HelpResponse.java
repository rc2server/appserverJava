package edu.wvu.stat.rc2.ws.response;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HelpResponse extends BaseResponse {
	private final String _topic;
	private final List<Map<String,String>> _items;
	
	public HelpResponse(String topic, List<Map<String,String>> items) {
		super("help", 0);
		_topic = topic;
		_items = items;
	}
	
	@JsonProperty
	public String getTopic() { return _topic; }
	
	@JsonProperty
	public List<Map<String,String>> getItems() { return _items; }
}
