package edu.wvu.stat.rc2;

import edu.wvu.stat.rc2.config.DatabaseConfig;

public class UnitTestDBConfig implements DatabaseConfig {

	@Override
	public String getDbhost() {
		return "localhost";
	}

	@Override
	public String getDbname() {
		return "rc2test";
	}

	@Override
	public String getDbuser() {
		return "rc2";
	}

	@Override
	public int getDbport() {
		return 0;
	}

}
