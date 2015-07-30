package edu.wvu.stat.rc2.resources;

import org.skife.jdbi.v2.DBI;
import static org.mockito.Mockito.*;

import edu.wvu.stat.rc2.persistence.PGDataSourceFactory;
import edu.wvu.stat.rc2.persistence.RCUser;

public abstract class BaseResourceTest {
	static final PGDataSourceFactory dbfactory = new PGDataSourceFactory();
	DBI _dbi;
	static final RCUser user = mock(RCUser.class);

	{
		when(user.getEmail()).thenReturn("cornholio@stat.wvu.edu");
		when(user.getFirstName()).thenReturn("Great");
		when(user.getLastName()).thenReturn("Cornholio");
		when(user.getLogin()).thenReturn("cornholio");
		when(user.getId()).thenReturn(1);
		when(user.isAdmin()).thenReturn(false);
		when(user.isEnabled()).thenReturn(true);
	}
	
	public BaseResourceTest() {
		_dbi = new DBI(dbfactory.getDataSource());
		
	}
}
