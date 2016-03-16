package edu.wvu.stat.rc2.ws;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import edu.wvu.stat.rc2.RCError;

public class SessionError implements RCError {
	int _codeNumber;
	String _message;
	
	//nothing should ever be all uppercase
	public enum ErrorCode {
		NoSuchFile(1001, "invalid file specified"),
		VersionMismatch(1002, "file version mismatch"),
		DatabaseUpdateFailed(1003, "failed to update database")
		;
		private int _codeNumber;
		private String _msg;
		
		ErrorCode(int c, String msg) { _codeNumber = c; _msg = msg; }
		
		@JsonCreator
		public static ErrorCode codeForValue(int code) {
			for (ErrorCode aCode : ErrorCode.values()) {
				if (aCode.codeNumber() == code) {
					return aCode;
				}
			}
			return null;
		}
		
		@JsonValue
		public int codeNumber() { return _codeNumber; }
		
		public String message() { return _msg; }
	}
	
	@JsonCreator
	public SessionError(@JsonProperty("errorCode") int code, @JsonProperty("message") String msg) {
		_codeNumber = code;
		_message = msg;
	}
	
	@JsonIgnore
	public SessionError(@JsonProperty("error") ErrorCode error) {
		_codeNumber = error.codeNumber();
		_message = error.message();
	}
	
	@Override
	@JsonProperty
	public int getErrorCode() { return _codeNumber; }

	@Override
	@JsonProperty
	public String getMessage() { return _message; }

}
