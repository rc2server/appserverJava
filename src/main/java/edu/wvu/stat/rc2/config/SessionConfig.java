package edu.wvu.stat.rc2.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public interface SessionConfig {
	/** the maximum size for a file whose contents will be included in a ShowOutputResponse websocket message */
	@JsonProperty
	int getShowOutputFileSizeLimitInKB();
	
	@JsonIgnore
	default int getShowOutputFileSizeLimitInBytes() {
		return getShowOutputFileSizeLimitInKB() * 1024;
	}
}
