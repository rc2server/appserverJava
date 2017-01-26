package edu.wvu.stat.rc2.ws;


import static edu.wvu.stat.rc2.Rc2AppConfiguration.UserSessionKey;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wvu.stat.rc2.Rc2CommonMocks;
import edu.wvu.stat.rc2.UnitTestDBConfig;
import edu.wvu.stat.rc2.config.SessionConfig;
import edu.wvu.stat.rc2.config.SessionConfigImpl;
import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.persistence.Rc2DataSourceFactory;
import edu.wvu.stat.rc2.ws.RCSessionCache.RWorkerFactory;

public class RCSessionServletTest {
	Rc2DataSourceFactory _dbfactory;
	ObjectMapper _mapper;
	WebSocketCreator _creator;
	RCUser _user;
	WebSocketServletFactory _wsFactory;
	ServletUpgradeRequest _upgradeRequest;
	HttpServletRequest _httpRequest;
	RCSessionCache _sessionCache;
	
	@Before
	public void setUp() throws Exception {
		_dbfactory = new Rc2DataSourceFactory(new UnitTestDBConfig());

		_sessionCache = new RCSessionCache(_dbfactory, new ObjectMapper(), new SessionConfigImpl(),
				new RWorkerFactory(new Rc2CommonMocks.MockSocketFactory()));
		_user = Rc2CommonMocks.mockTestUser();

		//we need to mock WebSocketServletFactory to save the creator in _creator when setCreator is called
		_wsFactory = mock(WebSocketServletFactory.class);
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				_creator = (WebSocketCreator) args[0];
				return null;
			}
		}).when(_wsFactory).setCreator(anyObject());
		//mock policy to allow setting idle timeout
		WebSocketPolicy policy = mock(WebSocketPolicy.class);
		when(_wsFactory.getPolicy()).thenReturn(policy);
//		verify(policy).setIdleTimeout(anyLong());

		//prep mocks to create websocket
		_upgradeRequest = mock(ServletUpgradeRequest.class);
		_httpRequest = mock(HttpServletRequest.class);
		when(_upgradeRequest.getHttpServletRequest()).thenReturn(_httpRequest);
		when(_httpRequest.getParameter("client")).thenReturn("webclient");
	}

	@Test
	public void testCreateServlet() {
		when(_httpRequest.getAttribute(UserSessionKey)).thenReturn(_user);
		when(_upgradeRequest.getRequestPath()).thenReturn("1");
		RCSessionServlet servlet = new RCSessionServlet(_sessionCache, new TestSessionConfig());
		servlet.configure(_wsFactory);
		assertThat(_creator, notNullValue());
		//create the websocket
		Object socket = _creator.createWebSocket(_upgradeRequest, null);
		assertThat(socket, is(instanceOf(RCSessionSocket.class)));
		
	}

	private void testSocketCreationError() {
		RCSessionServlet servlet = new RCSessionServlet(_sessionCache, new TestSessionConfig());
		servlet.configure(_wsFactory);
		assertThat(_creator, notNullValue());
		ServletUpgradeResponse response = mock(ServletUpgradeResponse.class);
		
		//create the websocket
		Object socket = _creator.createWebSocket(_upgradeRequest, response);
		assertThat(socket, nullValue());
		try {
			verify(response).sendError(eq(400), anyString());
		} catch (IOException e) {
			//should never happen
			e.printStackTrace();
			fail("io exception should never happen");
		}
	}
	
	@Test
	public void testCreateServletInvalidUser() {
		when(_httpRequest.getAttribute(UserSessionKey)).thenReturn(null);
		when(_upgradeRequest.getRequestPath()).thenReturn("1");
		testSocketCreationError();
	}

	@Test
	public void testCreateServletInvalidWorkspace() throws IOException {
		when(_httpRequest.getAttribute(UserSessionKey)).thenReturn(null);
		when(_upgradeRequest.getRequestPath()).thenReturn("11111111");
		testSocketCreationError();
	}
	
	class TestSessionConfig implements SessionConfig {

		@Override
		public int getShowOutputFileSizeLimitInKB() {
			return 20;
		}

		@Override
		public String getRComputeHost() {
			return "compute";
		}

		@Override
		public long getIdleTimeout() {
			return -1;
		}
		
	}
}
