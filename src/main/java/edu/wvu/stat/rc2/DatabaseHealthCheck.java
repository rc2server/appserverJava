package edu.wvu.stat.rc2;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.IntegerMapper;

import com.codahale.metrics.health.HealthCheck;

import edu.wvu.stat.rc2.persistence.Rc2DataSourceFactory;

public class DatabaseHealthCheck extends HealthCheck {
	private final Rc2DataSourceFactory dbFactory;
	
	public DatabaseHealthCheck(Rc2DataSourceFactory factory) {
		dbFactory = factory;
	}
	
	@Override
	protected Result check() throws Exception {
		DBI dbi = dbFactory.createDBI();
		Handle h = dbi.open();
		int userCount = h.createQuery("select count from rcuser").map(IntegerMapper.FIRST).list().get(0);
		if (userCount > 0)
			return Result.healthy();
		return Result.unhealthy("failed to get user count from database");
	}

}
