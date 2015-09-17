package edu.wvu.stat.rc2.ws;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.fail;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wvu.stat.rc2.persistence.Rc2DataSourceFactory;
import edu.wvu.stat.rc2.Rc2CommonMocks;
import edu.wvu.stat.rc2.persistence.RCUser;

public class RCSessionCacheTest {
	static final Rc2DataSourceFactory dbfactory = new Rc2DataSourceFactory();

	RCUser _user;
	ObjectMapper _mapper;
	
	@Before
	public void setUp() throws Exception {
		_mapper = new ObjectMapper();
		_user = Rc2CommonMocks.mockTestUser();
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