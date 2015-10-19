package edu.wvu.stat.rc2.ws;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wvu.stat.rc2.ws.request.*;

public class ClientRequestResolverTest {
	private ObjectMapper _mapper;
	
	@Before
	public void setUp() throws Exception {
		_mapper = new ObjectMapper();
	}

	@Test
	public void testKeepAliveRequest() throws Exception {
		final String json = "{\"msg\":\"keepAlive\"}";
		BaseRequest baseReq = _mapper.readValue(json, BaseRequest.class);
		assertThat(baseReq, instanceOf(KeepAliveRequest.class));
	}

	@Test
	public void testHelpRequest() throws Exception {
		final String json = "{\"msg\":\"help\", \"topic\":\"print\"}";
		BaseRequest baseReq = _mapper.readValue(json, BaseRequest.class);
		assertThat(baseReq, instanceOf(HelpRequest.class));
		HelpRequest helpReq = (HelpRequest)baseReq;
		assertThat(helpReq.getTopic(), is("print"));
	}

	@Test
	public void testExecuteRequest() throws Exception {
		final String json = "{\"msg\":\"execute\",\"code\":\"print;\",\"fileId\":11}";
		BaseRequest baseReq = _mapper.readValue(json, BaseRequest.class);
		assertThat(baseReq, instanceOf(ExecuteRequest.class));
		ExecuteRequest execReq = (ExecuteRequest)baseReq;
		assertThat(execReq.getFileId(), is(11));
		assertThat(execReq.getCode(), is("print;"));
	}
	
	@Test
	public void testGetVariableRequest() throws Exception {
		final String json = "{\"msg\":\"getVariable\",\"variable\":\"foo\"}";
		BaseRequest baseReq = _mapper.readValue(json, BaseRequest.class);
		assertThat(baseReq, instanceOf(GetVariableRequest.class));
		GetVariableRequest getReq = (GetVariableRequest)baseReq;
		assertThat(getReq.getVariable(), is("foo"));
	}
	
	@Test
	public void testWatchVariablesRequest() throws Exception {
		final String json = "{\"msg\":\"watchVariables\",\"watch\":true}";
		BaseRequest baseReq = _mapper.readValue(json, BaseRequest.class);
		assertThat(baseReq, instanceOf(WatchVariablesRequest.class));
		WatchVariablesRequest wReq = (WatchVariablesRequest)baseReq;
		assertThat(wReq.getWatch(), is(true));
	}
	
	@Test
	public void testUserListRequest() throws Exception {
		final String json = "{\"msg\":\"userList\"}";
		BaseRequest baseReq = _mapper.readValue(json, BaseRequest.class);
		assertThat(baseReq, instanceOf(UserListRequest.class));
	}
}

