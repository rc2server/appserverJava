package edu.wvu.stat.rc2.ws;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wvu.stat.rc2.Rc2CommonMocks;
import edu.wvu.stat.rc2.Rc2CommonMocks.RCMockDBObjects;
import edu.wvu.stat.rc2.persistence.RCFile;
import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.persistence.RCWorkspace;
import edu.wvu.stat.rc2.rworker.RWorker;
import edu.wvu.stat.rc2.ws.request.SaveRequest;
import edu.wvu.stat.rc2.ws.response.SaveResponse;

public class RCSessionTest {
	RCMockDBObjects _dbObjs;
	RCWorkspace _wspace;
	RWorker _worker;
	RCUser _user;
	RCSession _session;
	RCSessionSocket _socket;
	RemoteEndpoint _remoteEndpoint;
	
	@Before
	public void setUp() throws Exception {
		_wspace = Rc2CommonMocks.mockWorkspace();
		_dbObjs = Rc2CommonMocks.mockDBObjectsForSession(_wspace);
		_worker = new RWorker(new Rc2CommonMocks.MockSocketFactory(), null);

		_user = Rc2CommonMocks.mockTestUser();
		_session = new RCSession(_dbObjs.factory, null, _wspace.getId(), _worker);
		assertThat(_session.getWorkspaceId(), is(1));

		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		when(httpRequest.getParameter("client")).thenReturn("osx");
		ServletUpgradeRequest upRequest = mock(ServletUpgradeRequest.class);
		when(upRequest.getHttpServletRequest()).thenReturn(httpRequest);
		
		_socket = new RCSessionSocket(upRequest, _session, _session.getObjectMapper(), _user);
		
		org.eclipse.jetty.websocket.api.Session mockSession = mock(org.eclipse.jetty.websocket.api.Session.class);
		_remoteEndpoint = spy(RemoteEndpoint.class);
		when(mockSession.getRemote()).thenReturn(_remoteEndpoint);
		doNothing().when(_remoteEndpoint).sendString(anyString());
		Callable<Void> call = new Callable<Void>() {
			public Void call() { return null; }
		};
		Future<Void> strFuture = new FutureTask<Void>(call);
		doReturn(strFuture).when(_remoteEndpoint).sendStringByFuture(anyString());
		_socket.onOpen(mockSession);
	}
	
	@After
	public void tearDown() {
		try {
			_session.shutdown();
			Thread.sleep(200);
		} catch (Throwable e) {
		}
	}

	@Test
	public void testInvalidWorkspace() {
		try {
			new RCSession(_dbObjs.factory, null, 10111131, null);
			fail("failed to throw illegalArgumentException");
		} catch (IllegalArgumentException e) {
			//what we expected
		}
	}

	@Test
	public void testValidWorkspace() throws IOException {
		assertThat(_session.getClientCount(), is(1));
	}
	
	@Test
	public void testSaveRequest() throws Exception {
		String content = "#testing\n2*4\ny <- c(1,2,y)\n";
		String transid = "foo bar";
		RCFile origFile = _wspace.getFiles().get(0);
		RCFile modFile = RCFile.create(origFile.getId(), origFile.getWspaceId(), origFile.getName(), 
				origFile.getVersion()+1, origFile.getDateCreated(), new java.util.Date(), content.length());
		when(_dbObjs.fdao.findById(origFile.getId())).thenReturn(origFile).thenReturn(modFile);
		when(_dbObjs.fdao.updateFileContents(anyInt(), anyObject())).thenReturn(modFile);
		
		SaveRequest req = new SaveRequest(1, transid, origFile.getId(), origFile.getVersion(), content);
		ObjectMapper mapper = new ObjectMapper(new MessagePackFactory());
		byte[] reqBytes = mapper.writeValueAsBytes(req);
		ArgumentCaptor<ByteBuffer> captor = ArgumentCaptor.forClass(ByteBuffer.class);
		_session.processWebsocketBinaryMessage(_socket, reqBytes, 0, reqBytes.length);
		
		System.err.println("verifying");
		verify(_remoteEndpoint).sendBytesByFuture(captor.capture());
		ByteBuffer readBuf = captor.getValue();
		byte[] readBytes = new byte[readBuf.capacity()];
		readBuf.get(readBytes);
		SaveResponse rsp = mapper.readValue(readBytes, SaveResponse.class);
		assertThat(rsp.getTransId(), is(transid));
		assertThat(rsp.getSuccess(), is(true));
		assertThat(rsp.getFile().getId(), is(modFile.getId()));
		assertThat(rsp.getFile().getFileSize(), is(modFile.getFileSize()));
	}
	
}
