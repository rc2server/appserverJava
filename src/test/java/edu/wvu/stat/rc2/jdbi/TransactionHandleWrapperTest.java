package edu.wvu.stat.rc2.jdbi;

import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;

import edu.wvu.stat.rc2.persistence.PGDataSourceFactory;

public class TransactionHandleWrapperTest {
	static final PGDataSourceFactory dbfactory = new PGDataSourceFactory();
	private DBI _dbi;


	@Before
	public void setUp() throws Exception {
		_dbi = dbfactory.createDBI();
	}

	@Test(expected=UnableToExecuteStatementException.class)
	public void testExceptionOnDuplicateInsert() {
		
		try (TransactionHandleWrapper trans = new TransactionHandleWrapper(_dbi)) {
			//create a bogus workspace
			trans._h.execute("insert into rcworkspace (id,name,userId) values(1,'doofus',1)");
		}
	}

}
