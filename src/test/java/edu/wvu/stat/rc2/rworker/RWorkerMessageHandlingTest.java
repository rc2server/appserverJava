package edu.wvu.stat.rc2.rworker;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
//import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;

import edu.wvu.stat.rc2.ws.resposne.ErrorResponse;

public class RWorkerMessageHandlingTest {
	private TestDelegate delegate;
	private RWorker worker;
	
	@Before
	public void setUp() throws Exception {
		this.delegate = new TestDelegate();
		this.worker = new RWorker(null, this.delegate);
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

}
