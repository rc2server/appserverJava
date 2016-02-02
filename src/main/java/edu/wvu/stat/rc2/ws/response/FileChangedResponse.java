package edu.wvu.stat.rc2.ws.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.wvu.stat.rc2.persistence.RCFile;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FileChangedResponse extends BaseResponse {
	public enum ChangeType {
		Insert, Update, Delete
	}
	
	private final RCFile _file;
	private final ChangeType _change;
	
	@JsonCreator
	public FileChangedResponse(@JsonProperty("file") RCFile file, @JsonProperty("type") ChangeType type) {
		super("filechanged", 0);
		_file = file;
		_change = type;
	}
	
	@JsonProperty
	public RCFile getFile() { return _file; }
	
	@JsonProperty
	public ChangeType getType() { return _change; }
}
