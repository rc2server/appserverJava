package edu.wvu.stat.rc2.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DatabaseConfigImpl implements DatabaseConfig {
	private String _dbhost = "localhost";
	private String _dbname = "rc2";
	private String _dbuser = "rc2";
	private int _dbport = 0;
	
	@JsonProperty
	public String getDbhost() {
		return _dbhost;
	}
	public void setDbhost(String dbhost) {
		_dbhost = dbhost;
	}

	@JsonProperty
	public String getDbname() {
		return _dbname;
	}
	public void setDbname(String dbname) {
		_dbname = dbname;
	}

	@JsonProperty
	public String getDbuser() {
		return _dbuser;
	}
	public void setDbuser(String dbuser) {
		_dbuser = dbuser;
	}

	@JsonProperty
	public int getDbport() {
		return _dbport;
	}
	public void setDbport(int dbport) {
		_dbport = dbport;
	}
	

}
