package edu.wvu.stat.rc2.ws.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.wvu.stat.rc2.persistence.RCFile;
import edu.wvu.stat.rc2.ws.SessionError;
import edu.wvu.stat.rc2.ws.request.FileRequest.FileOperation;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FileOperationResponse extends BaseResponse {

	private final String _transId;
	private final FileOperation _op;
	private final boolean _success;
	private final RCFile _file;
	private final SessionError _error;
	
	@JsonCreator
	public FileOperationResponse(@JsonProperty("transId") String transId,
			@JsonProperty("operation") FileOperation operation,
			@JsonProperty("success") boolean success, 
			@JsonProperty("file") RCFile file, 
			@JsonProperty ("error") SessionError error) 
	{
		super("fileOpResponse", 0);
		_transId = transId;
		_op = operation;
		_success = success;
		_file = file;
		_error = error;
	}

	@JsonProperty
	public String getTransId() { return _transId; }

	@JsonProperty
	public FileOperation getOperation() { return _op; }
	
	@JsonProperty
	public boolean getSuccess() { return _success; }
	
	@JsonProperty 
	public RCFile getFile() { return _file; } 

	@JsonProperty
	public SessionError getError() { return _error; }
}
