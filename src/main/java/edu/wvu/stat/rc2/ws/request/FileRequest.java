package edu.wvu.stat.rc2.ws.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FileRequest extends BaseRequest {
	private final FileOperation _op;
	private final int _fileId;
	private final int _fileVersion;
	private final String _newName;
	private final String _transId;
	
	@JsonCreator
	public FileRequest(
			@JsonProperty("operation") FileOperation operation, 
			@JsonProperty("transId") String transId, 
			@JsonProperty("fileVersion") int fileVersion, 
			@JsonProperty("fileId") int fileId,
			@JsonProperty("newName") String newName) 
	{
		super("fileop");
		_op = operation;
		_fileId = fileId;
		_transId = transId;
		_fileVersion = fileVersion;
		_newName = newName;
	}

	@JsonProperty
	public FileOperation getOperation() { return _op; }
	
	@JsonProperty
	public String getTransId() { return _transId; }

	@JsonProperty
	public int getFileId() { return _fileId; }

	@JsonProperty
	public int getFileVersion() { return _fileVersion; }

	@JsonProperty
	public String getNewName() { return _newName; }
	
	@Override
	public String toString() {
		return String.format("operation %s, file: %d", _op, _fileId);
	}
	
	public enum FileOperation {
		REMOVE("rm"), RENAME("rename"), DUPLICATE("duplicate");
		
		private String _type;
		private FileOperation(String type) {
			_type = type;
		}
		
		@JsonCreator
		public static FileOperation fromValue(String value) {
			//probably should enumerate all enums and return one with matching operationType
			if (value.equals("rm"))
				return REMOVE;
			if (value.equals("rename"))
				return RENAME;
			if (value.equals("duplicate"))
				return DUPLICATE;
			throw new RuntimeException("invalid file operation:" + value);
		}
		
		@JsonValue
		public String getOperationType() { return _type; }
	}
}
