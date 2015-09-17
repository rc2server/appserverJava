package edu.wvu.stat.rc2.ws;

//import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.tweak.HandleCallback;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wvu.stat.rc2.persistence.RCUser;

public class RCSessionSocketTest {
	ServletUpgradeRequest _upRequest;
	
	@Before
	public void setUp() throws Exception {
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		when(httpRequest.getParameter("client")).thenReturn("osx");
		_upRequest = mock(ServletUpgradeRequest.class);
		when(_upRequest.getHttpServletRequest()).thenReturn(httpRequest);
	}

	@After
	public void tearDown() throws Exception {
		_upRequest = null;
	}
	
	@Test
	public void testWebSocket() throws Exception {
		RCUser user = mock(RCUser.class);
		when(user.getId()).thenReturn(1);
		when(user.getLogin()).thenReturn("beavis");
		MyWSDelegate delegate = new MyWSDelegate(1);
		RCSessionSocket socket = new RCSessionSocket(_upRequest, delegate, new ObjectMapper(), user);
		
		assertThat(socket.getUserId(), is(1));
		assertThat(socket.getDisplayName(), is("beavis"));
		assertThat(socket.getSocketId(), is(greaterThanOrEqualTo(0)));
		assertThat(socket.getConnectTime(), lessThan(System.currentTimeMillis()));

		org.eclipse.jetty.websocket.api.Session mockSession = mock(org.eclipse.jetty.websocket.api.Session.class);
		RemoteEndpoint endMock = spy(RemoteEndpoint.class);
		when(mockSession.getRemote()).thenReturn(endMock);
		doNothing().when(endMock).sendString(anyString());
	
		socket.onOpen(mockSession);
		assertThat(delegate.messages.size(), is(1));
		assertThat(delegate.messages.get(0), is("opened"));
		
		socket.onClose(0, "");
		assertThat(delegate.messages.size(), is(2));
		assertThat(delegate.messages.get(1), is("closed"));
	}

	public class MyWSDelegate implements RCSessionSocket.Delegate {
		final int _wspaceId;
		public final ArrayList<String> messages = new ArrayList<String>();
		
		MyWSDelegate(int wspaceId) {
			_wspaceId = wspaceId;
		}
		
		@Override
		public int getWorkspaceId() { return _wspaceId; }
		
		@Override
		public void websocketUseDatabaseHandle(HandleCallback<Void> callback) {
		}
		
		@Override
		public void websocketOpened(RCSessionSocket socket) {
			messages.add("opened");
		}
		
		@Override
		public void websocketClosed(RCSessionSocket socket) {
			messages.add("closed");
		}
		
		@Override
		public void processWebsocketMessage(RCSessionSocket socket, Map<String, Object> msg) {
			messages.add("msg");
		}
		
		@Override
		public void processWebsocketBinaryMessage(RCSessionSocket socket, byte[] data, int offset, int length) {
			messages.add("binary");
		}
		
		@Override
		public Map<String, Object> getSessionDescriptionForWebsocket(RCSessionSocket socket) {
			return new HashMap<String,Object>();
		}
		
	}
}
