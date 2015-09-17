package edu.wvu.stat.rc2.resources;

import org.skife.jdbi.v2.DBI;

import edu.wvu.stat.rc2.persistence.Rc2DataSourceFactory;
import edu.wvu.stat.rc2.Rc2CommonMocks;
import edu.wvu.stat.rc2.persistence.RCUser;

public abstract class BaseResourceTest {
	static final Rc2DataSourceFactory dbfactory = new Rc2DataSourceFactory();
	DBI _dbi;
	
	static final RCUser user = Rc2CommonMocks.mockTestUser();

	public BaseResourceTest() {
		_dbi = dbfactory.createDBI();
		
	}
}
