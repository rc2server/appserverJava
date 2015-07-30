package edu.wvu.stat.rc2.resources;

import org.skife.jdbi.v2.DBI;

import edu.wvu.stat.rc2.persistence.PGDataSourceFactory;
import edu.wvu.stat.rc2.persistence.RCUser;

public abstract class BaseResourceTest {
	static final PGDataSourceFactory dbfactory = new PGDataSourceFactory();
	DBI _dbi;
	RCUser _user;
	
	public BaseResourceTest() {
		_dbi = new DBI(dbfactory.getDataSource());
		RCUser.UserQueries dao = _dbi.onDemand(RCUser.UserQueries.class);
		_user = dao.findById(1);
	}
}
