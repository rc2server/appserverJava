package edu.wvu.stat.rc2.ws.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.wvu.stat.rc2.persistence.RCFile;

public class ShowOutputResponse extends BaseResponse {
	private final RCFile _file;
	private final byte[] _fileData;

	public ShowOutputResponse(int queryId, RCFile file, byte[] fileData) {
		super("showOutput", queryId);
		_file = file;
		_fileData = fileData;
	}
	
	@Override
	public boolean isBinaryMessage() { return _fileData != null; }

	@JsonProperty
	public RCFile getFile() { return _file; }

	@JsonProperty
	public byte[] getFileData() { return _fileData; }
}
