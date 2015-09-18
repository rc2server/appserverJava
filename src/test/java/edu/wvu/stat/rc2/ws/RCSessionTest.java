package edu.wvu.stat.rc2.ws;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import edu.wvu.stat.rc2.Rc2CommonMocks;
import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.persistence.RCWorkspace;
import edu.wvu.stat.rc2.persistence.Rc2DAO;
import edu.wvu.stat.rc2.persistence.Rc2DataSourceFactory;

public class RCSessionTest {
	Rc2DataSourceFactory _dbfactory;
	Rc2DAO _dao;
	RCWorkspace _wspace;
	
	@Before
	public void setUp() throws Exception {
		_dbfactory = mock(Rc2DataSourceFactory.class);
		_dao = mock(Rc2DAO.class);
		_wspace = Rc2CommonMocks.mockWorkspace();
		when(_dbfactory.createDAO()).thenReturn(_dao);
		when(_dao.findWorkspaceById(1)).thenReturn(_wspace);
}

	@Test
	public void testInvalidWorkspace() {
		try {
			new RCSession(_dbfactory, null, 10111131);
			fail("failed to throw illegalArgumentException");
		} catch (IllegalArgumentException e) {
			//what we exepcted
		}
	}

	@Test
	public void testValidWorkspace() throws IOException {
		RCUser user = Rc2CommonMocks.mockTestUser();
		RCSession session = new RCSession(_dbfactory, null, _wspace.getId());
		assertThat(session.getWorkspaceId(), is(1));

		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		when(httpRequest.getParameter("client")).thenReturn("osx");
		ServletUpgradeRequest upRequest = mock(ServletUpgradeRequest.class);
		when(upRequest.getHttpServletRequest()).thenReturn(httpRequest);
		
		RCSessionSocket socket = new RCSessionSocket(upRequest, session, session.getObjectMapper(), user);
		
		org.eclipse.jetty.websocket.api.Session mockSession = mock(org.eclipse.jetty.websocket.api.Session.class);
		RemoteEndpoint endMock = spy(RemoteEndpoint.class);
		when(mockSession.getRemote()).thenReturn(endMock);
		doNothing().when(endMock).sendString(anyString());
		
		socket.onOpen(mockSession);
		assertThat(session.getClientCount(), is(1));
	}
}
