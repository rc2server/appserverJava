package edu.wvu.stat.rc2.ws;

import edu.wvu.stat.rc2.persistence.PGDataSourceFactory;
import edu.wvu.stat.rc2.persistence.RCWorkspace;

public final class RCSession {
	private PGDataSourceFactory _dbfactory;
	private RCWorkspace _wspace;
	
	/**
	 @param dbfactory A factory is passed so that if the connection is dropped for some reason, a new one can be opened.


	 */
	public RCSession(PGDataSourceFactory dbfactory, RCWorkspace workspace) {
		_dbfactory = dbfactory;
		_wspace = workspace;
	}
	
	
}
