package edu.wvu.stat.rc2.ws.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.wvu.stat.rc2.persistence.RCFile;
import edu.wvu.stat.rc2.ws.SessionError;

public class SaveResponse extends BaseResponse {
	
	private String _transId;
	private RCFile _file;
	private boolean _success;
	private SessionError _error;
	
	@JsonCreator
	public SaveResponse(@JsonProperty("transId") String transId, 
			@JsonProperty("success") boolean success, 
			@JsonProperty("file") RCFile file, 
			@JsonProperty ("error") SessionError error) 
	{
		super("saveResponse", 0);
		_transId = transId;
		_success = success;
		_file = file;
		_error = error;
	}
	
	@Override
	@JsonIgnore
	public boolean isBinaryMessage() { return true; }
	
	@JsonProperty
	public String getTransId() { return _transId; }

	@JsonProperty
	public boolean getSuccess() { return _success; }
	
	@JsonProperty 
	public RCFile getFile() { return _file; } 

	@JsonProperty
	public SessionError getError() { return _error; }
}
