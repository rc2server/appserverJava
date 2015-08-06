package edu.wvu.stat.rc2.persistence;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.Random;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

public class RCWorkspaceDataTest {
	static final PGDataSourceFactory dbfactory = new PGDataSourceFactory();
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
		
		RCWorkspace.Queries dao = _dbi.onDemand(RCWorkspace.Queries.class);
		RCWorkspaceData.Queries dataDao = _dbi.onDemand(RCWorkspaceData.Queries.class);
		int wsId = dao.createWorkspace("testwsdata", 1);
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
