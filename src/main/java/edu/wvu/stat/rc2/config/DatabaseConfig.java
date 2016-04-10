package edu.wvu.stat.rc2.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface DatabaseConfig {
	@JsonProperty
	public String getDbhost();

	@JsonProperty
	public String getDbname();

	@JsonProperty
	public String getDbuser();

	@JsonProperty
	public int getDbport();
}
