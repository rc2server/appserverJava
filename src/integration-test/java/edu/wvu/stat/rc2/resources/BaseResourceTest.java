package edu.wvu.stat.rc2.resources;

import edu.wvu.stat.rc2.persistence.Rc2DataSourceFactory;
import edu.wvu.stat.rc2.Rc2CommonMocks;
import edu.wvu.stat.rc2.UnitTestDBConfig;
import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.persistence.Rc2DAO;

public abstract class BaseResourceTest {
	static final Rc2DataSourceFactory dbfactory = new Rc2DataSourceFactory(new UnitTestDBConfig());
	static final Rc2DAO _dao = dbfactory.createDAO();
	
	static final RCUser user = Rc2CommonMocks.mockTestUser();

	public BaseResourceTest() {
	}
}
