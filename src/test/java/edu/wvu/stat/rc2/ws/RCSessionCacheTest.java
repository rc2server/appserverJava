package edu.wvu.stat.rc2.ws;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.fail;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wvu.stat.rc2.persistence.PGDataSourceFactory;
import edu.wvu.stat.rc2.persistence.RCUser;

public class RCSessionCacheTest {
	static final PGDataSourceFactory dbfactory = new PGDataSourceFactory();

	DBI _dbi;
	ObjectMapper _mapper;
	RCUser _user;
	
	@Before
	public void setUp() throws Exception {
		_dbi = dbfactory.createDBI();
		_mapper = new ObjectMapper();
		RCUser.Queries userDao = _dbi.onDemand(RCUser.Queries.class);
		_user = userDao.findById(1);
	}

	RCSessionSocket createSocketForWorkspace(int wspaceId) {
		RCSessionCache cache = new RCSessionCache(dbfactory, _mapper);
		
		ServletUpgradeRequest req = mock(ServletUpgradeRequest.class);
		HttpServletRequest hreq = mock(HttpServletRequest.class);
		when(req.getHttpServletRequest()).thenReturn(hreq);
		when(hreq.getParameter("client")).thenReturn("webclient");
		
		return cache.socketForWorkspaceAndUser(req, wspaceId, _user);
	}
	
	@Test
	public void testSessionAndSocketCreation() {
		RCSessionSocket socket = createSocketForWorkspace(1);
		assertThat(socket.getDelegate().getWorkspaceId(), is(1));
	}

	@Test
	public void testInvalidSessionForWorkspaceException() {
		try {
			this.createSocketForWorkspace(0);
			fail("exception not thrown");
		} catch (IllegalArgumentException e) {
		}
	}
}
