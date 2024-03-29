package edu.wvu.stat.rc2.persistence;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.Random;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import edu.wvu.stat.rc2.UnitTestDBConfig;

public class RCWorkspaceDataTest {
	static final Rc2DataSourceFactory dbfactory = new Rc2DataSourceFactory(new UnitTestDBConfig());
	private final Random _random;
	DBI _dbi;

	public RCWorkspaceDataTest() {
		_dbi = dbfactory.createDBI();
		_random = new Random();
	}
	
	@Test
	public void testWorkspaceData() {
		byte[] rawData = new byte[1024];
		_random.nextBytes(rawData);
		byte[] upData = new byte[512];
		_random.nextBytes(upData);
		
		RCWorkspaceQueries dao = _dbi.onDemand(RCWorkspaceQueries.class);
		RCWorkspaceData.Queries dataDao = _dbi.onDemand(RCWorkspaceData.Queries.class);
		int wsId = dao.createWorkspace("testwsdata", 101, 1);
		try {
			RCWorkspaceData wsData = dataDao.findById(wsId);
			assertNull(wsData);
			int result = dataDao.createData(wsId, rawData);
			assertThat(result, is(1));
			RCWorkspaceData data = dataDao.findById(wsId);
			assertThat(data.getData(), equalTo(rawData));
			result = dataDao.updateData(upData, wsId);
			assertThat(result, is(1));
			data = dataDao.findById(wsId);
			assertThat(data.getData(), equalTo(upData));
		} finally {
			dao.deleteWorkspace(wsId);
		}
		
	}
}
