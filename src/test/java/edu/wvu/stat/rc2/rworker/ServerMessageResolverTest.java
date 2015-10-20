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

import edu.wvu.stat.rc2.rworker.response.BaseMessage;
import edu.wvu.stat.rc2.rworker.response.ErrorMessage;
import edu.wvu.stat.rc2.rworker.response.ExecCompleteMessage;
import edu.wvu.stat.rc2.rworker.response.HelpMessage;
import edu.wvu.stat.rc2.rworker.response.ResultsMessage;
import edu.wvu.stat.rc2.rworker.response.ShowOutputMessage;
import edu.wvu.stat.rc2.rworker.response.VariableUpdateMessage;
import edu.wvu.stat.rc2.rworker.response.VariableValueMessage;

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
		jsonObj.put("helpTopic", "print");
		jsonObj.put("helpPath", Arrays.asList("path1","path2"));
		final String json = _mapper.writeValueAsString(jsonObj);
		BaseMessage baseMsg = _mapper.readValue(json, BaseMessage.class);
		assertThat(baseMsg, instanceOf(HelpMessage.class));
		HelpMessage msg = (HelpMessage)baseMsg;
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
		BaseMessage baseMsg = _mapper.readValue(json, BaseMessage.class);
		assertThat(baseMsg, instanceOf(ResultsMessage.class));
		ResultsMessage msg = (ResultsMessage)baseMsg;
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
		BaseMessage baseMsg = _mapper.readValue(json, BaseMessage.class);
		assertThat(baseMsg, instanceOf(VariableUpdateMessage.class));
		VariableUpdateMessage vuMsg = (VariableUpdateMessage)baseMsg;
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
		BaseMessage baseMsg = _mapper.readValue(json, BaseMessage.class);
		assertThat(baseMsg, instanceOf(VariableValueMessage.class));
		VariableValueMessage vvMsg = (VariableValueMessage)baseMsg;
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
		BaseMessage baseMsg = _mapper.readValue(json, BaseMessage.class);
		assertThat(baseMsg, instanceOf(ExecCompleteMessage.class));
		ExecCompleteMessage execMsg = (ExecCompleteMessage)baseMsg;
		assertThat(execMsg.getStartTime(), is(startTime));
		assertThat(execMsg.getImageIds(), is(imageIds));
		assertThat(execMsg.getImageBatchId(), is(11));
	}
	
	@Test
	public void testErrorMessage() throws Exception {
		final String details = "some details";
		ErrorMessage error = (ErrorMessage)_mapper.readValue(
				"{\"msg\":\"error\", \"errorCode\":100,\"errorDetails\":\"" + details + "\"}", 
				ErrorMessage.class);
		assertThat(error.getCode(), is(100));
		assertThat(error.getDetails(), is(details));
	}
	
	@Test
	public void testShowOutputMessage() throws Exception {
		final int fileId = 121;
		ShowOutputMessage msg = _mapper.readValue(
				"{\"msg\":\"showoutput\",\"fileId\":" + fileId + "}", 
				ShowOutputMessage.class);
		assertThat(msg.getFileId(), is(fileId));
	}
	
	@Test
	public void testGenericShowOutput() throws Exception {
		final int fileId = 121;
		BaseMessage obj = _mapper.readValue("{\"msg\":\"showoutput\",\"fileId\":" + fileId + "}", BaseMessage.class);
		assertThat(obj, instanceOf(ShowOutputMessage.class));
	}
}
