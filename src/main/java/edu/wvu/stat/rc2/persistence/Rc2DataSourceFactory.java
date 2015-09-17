package edu.wvu.stat.rc2.persistence;

import javax.sql.DataSource;

import org.skife.jdbi.v2.DBI;

import com.impossibl.postgres.jdbc.PGDataSource;

import edu.wvu.stat.rc2.jdbi.BigIntegerArgumentFactory;

public class Rc2DataSourceFactory {
	private DataSource _ds;
	
	public Rc2DataSourceFactory() {
		PGDataSource pgds=null;
		pgds = new PGDataSource();
		pgds.setUser("rc2");
		pgds.setDatabase("rc2test");
		pgds.setHost("localhost");
		_ds = pgds;
		if (_ds instanceof PGDataSource)
			pgds = (PGDataSource)_ds;
		pgds.setApplicationName("rc2 REST server");
	}
	
	public DBI createDBI() {
		DBI dbi = new DBI(_ds);
		dbi.registerArgumentFactory(new BigIntegerArgumentFactory());
		return dbi;
	}
	
	public Rc2DAO createDAO() {
		DBI dbi = createDBI();
		return new Rc2DAO(dbi);
	}
}
