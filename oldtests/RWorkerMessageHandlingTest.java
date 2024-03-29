package edu.wvu.stat.rc2.rworker;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.wvu.stat.rc2.persistence.RCSessionImage;
import edu.wvu.stat.rc2.persistence.Rc2DAO;
import edu.wvu.stat.rc2.rworker.ServerMessageResolver.Messages;
import edu.wvu.stat.rc2.ws.response.ErrorResponse;
import edu.wvu.stat.rc2.ws.response.ExecCompleteResponse;
import edu.wvu.stat.rc2.ws.response.HelpResponse;
import edu.wvu.stat.rc2.ws.response.ResultsResponse;
import edu.wvu.stat.rc2.ws.response.VariableResponse;

public class RWorkerMessageHandlingTest {
	private TestDelegate delegate;
	private RWorker worker;
	
	@Before
	public void setUp() throws Exception {
		this.delegate = new TestDelegate();
		this.worker = new RWorker(new MockSocketFactory(), this.delegate);
	}

	@After
	public void tearDown() throws Exception {
		this.worker.shutdown();
	}
	
	@Test 
	public void testVariableValueMessage() throws Exception {
		this.delegate.socketId = 11;
		Map<String,Object> valueMap = Collections.singletonMap("name", (Object)"foo");
		HashMap<String,Object> msg = new HashMap<String,Object>();
		msg.put("msg", Messages.VAR_VALUE_MSG.jsonValue);
		msg.put("value", valueMap);
		msg.put("userIdentifier", this.delegate.socketId);
		final String json = delegate.getObjectMapper().writeValueAsString(msg);
		worker.handleJsonResponse(json);
		assertThat(delegate.messagesBroadcast.size(), is(1));
		assertThat(delegate.messagesBroadcast.get(0), instanceOf(VariableResponse.class));
		VariableResponse rsp = (VariableResponse)delegate.messagesBroadcast.get(0);
		assertThat(rsp.getVariables(), is(valueMap));
		assertThat(rsp.isSingleValue(), is(true));
	}
	
	@Test 
	public void testVariableUpdateMessage() throws Exception {
		Map<String,Object> valueMap = Collections.singletonMap("name", (Object)"foo");
		HashMap<String,Object> msg = new HashMap<String,Object>();
		msg.put("msg", Messages.VAR_UPDATE_MSG.jsonValue);
		msg.put("variables", valueMap);
		msg.put("delta", true);
		final String json = delegate.getObjectMapper().writeValueAsString(msg);
		worker.handleJsonResponse(json);
		assertThat(delegate.messagesBroadcast.size(), is(1));
		assertThat(delegate.messagesBroadcast.get(0), instanceOf(VariableResponse.class));
		VariableResponse rsp = (VariableResponse)delegate.messagesBroadcast.get(0);
		assertThat(rsp.getVariables(), is(valueMap));
		assertThat(rsp.isSingleValue(), is(false));
		assertThat(rsp.isDelta(), is(true));
	}
	
	@Test
	public void testShowOutputMessage() {
		final String json = "{\"msg\":\"" + Messages.SHOW_OUTPUT_MSG.jsonValue + "\", \"fileId\":11}";
		worker.handleJsonResponse(json);
		assertThat(delegate.messagesBroadcast.size(), is(1));
		assertThat(delegate.messagesBroadcast.get(0), instanceOf(ResultsResponse.class));
		ResultsResponse msg = (ResultsResponse)delegate.messagesBroadcast.get(0);
		assertThat(msg.getFile().getId(), is(11));
	}

	@Test
	public void testResultsMessage() {
		final String json = "{\"msg\":\"" + Messages.RESULTS_MSG.jsonValue + "\", \"string\":\"foobar\"}";
		worker.handleJsonResponse(json);
		assertThat(delegate.messagesBroadcast.size(), is(1));
		assertThat(delegate.messagesBroadcast.get(0), instanceOf(ResultsResponse.class));
		ResultsResponse msg = (ResultsResponse)delegate.messagesBroadcast.get(0);
		assertThat(msg.getString(), is("foobar"));
	}
	
	@Test
	public void testErrorMessage() {
		final String errJson = "{\"msg\":\"error\", \"errorDetails\":\"test error\", \"errorCode\":101}";
		worker.handleJsonResponse(errJson);
		assertThat(delegate.messagesBroadcast.size(), is(1));
		assertThat(delegate.messagesBroadcast.get(0), instanceOf(ErrorResponse.class));
		ErrorResponse emsg = (ErrorResponse)delegate.messagesBroadcast.get(0);
		assertThat(emsg.getError(), is("test error"));
	}
	
	@Test
	public void testHelpMessage() throws Exception {
		final List<String> paths = Arrays.asList("/rc2/library/foo/bar.html", "/rc2/library/foo/baz.html");
		final String topic = "print";
		HashMap<String,Object> msg = new HashMap<String,Object>();
		msg.put("msg", Messages.HELP_MSG.jsonValue);
		msg.put("topic", topic);
		msg.put("paths", paths);
		worker.handleJsonResponse(delegate.getObjectMapper().writeValueAsString(msg));
		assertThat(delegate.messagesBroadcast.size(), is(1));
		assertThat(delegate.messagesBroadcast.get(0), instanceOf(HelpResponse.class));
		HelpResponse rsp = (HelpResponse)delegate.messagesBroadcast.get(0);
		assertThat(rsp.getTopic(), is(topic));
		assertThat(rsp.getTopic(), is(topic));
		assertThat(rsp.getItems().get(0).get("title"), is("bar (library)"));
	}
	
	@Test
	public void testExecCompleteMessage() throws Exception {
		byte[] imgData = new byte[1];
		final int batchId = 1002;
		List<RCSessionImage> fakeImages = Arrays.asList(
				RCSessionImage.create(101, delegate.sessionRecordId, batchId, 1, "foo.jpg", null, new Date(), imgData),
				RCSessionImage.create(102, delegate.sessionRecordId, batchId, 1, "bar.png", "image bar", new Date(), imgData)
			);
		delegate.dao = mock(Rc2DAO.class);
		when(delegate.dao.findImageBatchById(batchId, delegate.getSessionRecordId())).thenReturn(fakeImages);
		
		HashMap<String,Object> msg = new HashMap<String,Object>();
		msg.put("msg", Messages.EXEC_COMPLETE_MSG.jsonValue);
		msg.put("startTime", System.currentTimeMillis());
		msg.put("imgBatch", batchId);
		final String json = delegate.getObjectMapper().writeValueAsString(msg);
		worker.handleJsonResponse(json);
		assertThat(delegate.messagesBroadcast.size(), is(1));
		assertThat(delegate.messagesBroadcast.get(0), instanceOf(ExecCompleteResponse.class));
		ExecCompleteResponse rsp = (ExecCompleteResponse)delegate.messagesBroadcast.get(0);
		assertThat(rsp.getImageBatchId(), is(batchId));
		assertThat(rsp.getImages(), is(fakeImages));
	}

	class MockSocketFactory extends RWorker.SocketFactory {
		Socket mockSocket = mock(Socket.class);
		public Socket createSocket(String host, int port) throws UnknownHostException, IOException {
			return mockSocket;
		}
	}
}
