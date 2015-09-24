package edu.wvu.stat.rc2.rworker;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.wvu.stat.rc2.persistence.RCSessionImage;
import edu.wvu.stat.rc2.persistence.Rc2DAO;
import edu.wvu.stat.rc2.rworker.ServerMessageResolver.Messages;
import edu.wvu.stat.rc2.ws.resposne.ErrorResponse;
import edu.wvu.stat.rc2.ws.resposne.ExecCompleteResponse;

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
	
	@Test
	public void testExecCompleteMessage() throws Exception {
		byte[] imgData = new byte[1];
		final int batchId = 1002;
		List<RCSessionImage> fakeImages = Arrays.asList(
				RCSessionImage.create(101, delegate.sessionRecordId, batchId, "foo.jpg", new Date(), imgData),
				RCSessionImage.create(102, delegate.sessionRecordId, batchId, "bar.png", new Date(), imgData)
			);
		delegate.dao = mock(Rc2DAO.class);
		when(delegate.dao.findImageBatchById(batchId)).thenReturn(fakeImages);
		
//		List<Integer> modFiles = Arrays.asList(1,2,3);
//		List<Integer> delFiles = Arrays.asList(9,8,7);
		HashMap<String,Object> msg = new HashMap<String,Object>();
		msg.put("msg", Messages.EXEC_COMPLETE_MSG.jsonValue);
		msg.put("startTime", System.currentTimeMillis());
		msg.put("imgBatch", batchId);
//		msg.put("filesModified", modFiles);
//		msg.put("filesDeleted", delFiles);
		final String json = delegate.getObjectMapper().writeValueAsString(msg);
		worker.handleJsonResponse(json);
		assertThat(delegate.messagesBroadcast.size(), is(1));
		assertThat(delegate.messagesBroadcast.get(0), instanceOf(ExecCompleteResponse.class));
		ExecCompleteResponse rsp = (ExecCompleteResponse)delegate.messagesBroadcast.get(0);
		assertThat(rsp.getImageBatchId(), is(batchId));
		assertThat(rsp.getImages(), is(fakeImages));
	}

}
