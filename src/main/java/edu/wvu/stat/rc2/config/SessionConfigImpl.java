package edu.wvu.stat.rc2.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SessionConfigImpl implements SessionConfig {
	private int _showOutputFileSizeLimit = 20;
	private String _computeHost = "compute";
	
	@Override
	@JsonProperty
	public int getShowOutputFileSizeLimitInKB() {
		return _showOutputFileSizeLimit;
	}
	public void setShowOutputFileSizeLimitInKB(int limit) {
		_showOutputFileSizeLimit = limit;
	}

	@Override
	@JsonProperty
	public String getRComputeHost() {
		return _computeHost;
	}
	
	public void setRComputeHost(String host) {
		_computeHost = host;
	}
}
