package edu.wvu.stat.rc2.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SessionConfigImpl implements SessionConfig {
	private int _showOutputFileSizeLimit = 20;
	private String _computeHost = "compute";
	private long _idleTimeout;
	
	@Override
	@JsonProperty
	public long getIdleTimeout() {
		return _idleTimeout;
	}
	
	public void setIdleTimeout(long val) {
		_idleTimeout = val;
		if (_idleTimeout < -1) { _idleTimeout = -1; }
	}
	
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
