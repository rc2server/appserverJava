package edu.wvu.stat.rc2;

import io.dropwizard.Configuration;

public class Rc2AppConfiguration extends Configuration {
	public static final String UserSessionKey = "rc2user";
	public static final String LoginTokenKey = "rc2token";
	
	private boolean _prettyPrint;
	
	public boolean getPrettyPrint() { return _prettyPrint; }
	public void setPrettyPrint(boolean p) { _prettyPrint = p; }
}
