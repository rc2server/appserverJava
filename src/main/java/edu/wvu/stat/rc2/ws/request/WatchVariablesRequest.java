package edu.wvu.stat.rc2.ws.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WatchVariablesRequest extends BaseRequest {
	private final boolean _watch;
	
	@JsonCreator
	public WatchVariablesRequest(@JsonProperty("watch") boolean watch) {
		super("watchVariables");
		_watch = watch;
	}
	
	@JsonProperty
	public boolean getWatch() { return _watch; }
}
