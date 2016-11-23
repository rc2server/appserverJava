package edu.wvu.stat.rc2.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public interface SessionConfig {
	/** the maximum size for a file whose contents will be included in a ShowOutputResponse websocket message */
	@JsonProperty
	int getShowOutputFileSizeLimitInKB();
	
	/** The hostname for the compute server to connect to */
	@JsonProperty
	String getRComputeHost();
	
	/** the idle timeout value for a websocket */
	@JsonProperty
	long getIdleTimeout();
	
	@JsonIgnore
	default int getShowOutputFileSizeLimitInBytes() {
		return getShowOutputFileSizeLimitInKB() * 1024;
	}
}
