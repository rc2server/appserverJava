package edu.wvu.stat.rc2.rworker;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
//import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wvu.stat.rc2.rworker.response.BaseRResponse;
import edu.wvu.stat.rc2.rworker.response.ErrorRResponse;
import edu.wvu.stat.rc2.rworker.response.ExecCompleteRResponse;
import edu.wvu.stat.rc2.rworker.response.HelpRResponse;
import edu.wvu.stat.rc2.rworker.response.ResultsRResponse;
import edu.wvu.stat.rc2.rworker.response.ShowOutputRResponse;
import edu.wvu.stat.rc2.rworker.response.VariableUpdateRResponse;
import edu.wvu.stat.rc2.rworker.response.VariableValueRResponse;

public class ServerMessageResolverTest {
	private ObjectMapper _mapper;
	
	@Before
	public void setUp() throws Exception {
		_mapper = new ObjectMapper();
	}

	@Test
	public void testHelp() throws Exception {
		HashMap<String,Object> jsonObj = new HashMap<String,Object>();
		jsonObj.put("msg", "help");
		jsonObj.put("topic", "print");
		jsonObj.put("paths", Arrays.asList("path1","path2"));
		final String json = _mapper.writeValueAsString(jsonObj);
		BaseRResponse baseMsg = _mapper.readValue(json, BaseRResponse.class);
		assertThat(baseMsg, instanceOf(HelpRResponse.class));
		HelpRResponse msg = (HelpRResponse)baseMsg;
		assertThat(msg, is(not(nullValue())));
		assertThat(msg.getTopic(), is("print"));
		List<String> paths = msg.getPaths();
		assertThat(paths.size(), is(2));
		assertThat(paths.get(0), is("path1"));
		assertThat(paths.get(1), is("path2"));
	}
	
	@Test
	public void testResults() throws Exception {
		final String results = "foo bar baz";
		HashMap<String,Object> jsonObj = new HashMap<String,Object>();
		jsonObj.put("msg", "results");
		jsonObj.put("string", results);
		final String json = _mapper.writeValueAsString(jsonObj);
		BaseRResponse baseMsg = _mapper.readValue(json, BaseRResponse.class);
		assertThat(baseMsg, instanceOf(ResultsRResponse.class));
		ResultsRResponse msg = (ResultsRResponse)baseMsg;
		assertThat(msg.getString(), is(results));
	}
	
	@Test
	public void testVariableUpdate() throws Exception {
		HashMap<String,Object> vars= new HashMap<String,Object>();
		vars.put("foo", "bar");
		vars.put("id", 1.2);
		HashMap<String,Object> jsonObj = new HashMap<String,Object>();
		jsonObj.put("msg", "variableupdate");
		jsonObj.put("delta", true);
		jsonObj.put("variables", vars);
		final String json = _mapper.writeValueAsString(jsonObj);
		BaseRResponse baseMsg = _mapper.readValue(json, BaseRResponse.class);
		assertThat(baseMsg, instanceOf(VariableUpdateRResponse.class));
		VariableUpdateRResponse vuMsg = (VariableUpdateRResponse)baseMsg;
		assertThat(vuMsg.isDelta(), is(true));
		assertThat(vuMsg.getVariables().get("foo"), is("bar"));
		assertThat(vuMsg.getVariables().get("id"), is(1.2));
	}
	
	@Test
	public void testVariableValue() throws Exception {
		HashMap<String,Object> val = new HashMap<String,Object>();
		val.put("type", "integer");
		val.put("value", 12);
		HashMap<String,Object> jsonObj = new HashMap<String,Object>();
		jsonObj.put("msg", "variablevalue");
		jsonObj.put("value", val);
		final String json = _mapper.writeValueAsString(jsonObj);
		BaseRResponse baseMsg = _mapper.readValue(json, BaseRResponse.class);
		assertThat(baseMsg, instanceOf(VariableValueRResponse.class));
		VariableValueRResponse vvMsg = (VariableValueRResponse)baseMsg;
		assertThat(vvMsg.getValue().get("type"), is("integer"));
		assertThat(vvMsg.getValue().get("value"), is(12));
	}

	@Test
	public void testExecComplete() throws Exception {
		List<Integer> imageIds = Arrays.asList(2,3,4);
		long startTime = System.currentTimeMillis()-1200;
		HashMap<String,Object> jsonObj = new HashMap<String,Object>();
		jsonObj.put("msg", "execComplete");
		jsonObj.put("startTime", Long.toString(startTime));
		jsonObj.put("images", imageIds);
		jsonObj.put("imgBatch", 11);
		final String json = _mapper.writeValueAsString(jsonObj);
		BaseRResponse baseMsg = _mapper.readValue(json, BaseRResponse.class);
		assertThat(baseMsg, instanceOf(ExecCompleteRResponse.class));
		ExecCompleteRResponse execMsg = (ExecCompleteRResponse)baseMsg;
		assertThat(execMsg.getStartTime(), is(startTime));
		assertThat(execMsg.getImageIds(), is(imageIds));
		assertThat(execMsg.getImageBatchId(), is(11));
	}
	
	@Test
	public void testErrorMessage() throws Exception {
		final String details = "some details";
		ErrorRResponse error = (ErrorRResponse)_mapper.readValue(
				"{\"msg\":\"error\", \"errorCode\":100,\"errorDetails\":\"" + details + "\"}", 
				ErrorRResponse.class);
		assertThat(error.getCode(), is(100));
		assertThat(error.getDetails(), is(details));
	}
	
	@Test
	public void testShowOutputMessage() throws Exception {
		final int fileId = 121;
		ShowOutputRResponse msg = _mapper.readValue(
				"{\"msg\":\"showoutput\",\"fileId\":" + fileId + "}", 
				ShowOutputRResponse.class);
		assertThat(msg.getFileId(), is(fileId));
	}
	
	@Test
	public void testGenericShowOutput() throws Exception {
		final int fileId = 121;
		BaseRResponse obj = _mapper.readValue("{\"msg\":\"showoutput\",\"fileId\":" + fileId + "}", BaseRResponse.class);
		assertThat(obj, instanceOf(ShowOutputRResponse.class));
	}
}
