package edu.wvu.stat.rc2.resources;

import org.skife.jdbi.v2.DBI;
import static org.mockito.Mockito.*;

import edu.wvu.stat.rc2.persistence.PGDataSourceFactory;
import edu.wvu.stat.rc2.persistence.RCUser;

public abstract class BaseResourceTest {
	static final PGDataSourceFactory dbfactory = new PGDataSourceFactory();
	DBI _dbi;
	RCUser _user;
	
	public BaseResourceTest() {
		_dbi = new DBI(dbfactory.getDataSource());
		
		_user = mock(RCUser.class);
		when(_user.getEmail()).thenReturn("cornholio@stat.wvu.edu");
		when(_user.getFirstName()).thenReturn("Great");
		when(_user.getLastName()).thenReturn("Cornholio");
		when(_user.getLogin()).thenReturn("cornholio");
		when(_user.getId()).thenReturn(1);
		when(_user.isAdmin()).thenReturn(false);
		when(_user.isEnabled()).thenReturn(true);
	}
}
