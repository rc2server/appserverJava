package edu.wvu.stat.rc2;

import io.dropwizard.Configuration;

public class Rc2AppConfiguration extends Configuration {
	private boolean _prettyPrint;
	
	public boolean getPrettyPrint() { return _prettyPrint; }
	public void setPrettyPrint(boolean p) { _prettyPrint = p; }
}
