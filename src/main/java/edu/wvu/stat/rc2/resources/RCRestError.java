package edu.wvu.stat.rc2.resources;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.wvu.stat.rc2.RCError;

public enum RCRestError implements RCError  {
	DuplicateName(100, 422, "DuplicateName");
	
	private static final ResourceBundle rBundle = ResourceBundle.getBundle("RCRestError");
	private int _errorCode, _httpCode;
	private String _msgKey;
	
	@JsonCreator
	RCRestError(@JsonProperty("errorCode") int errorCode, @JsonProperty("httpCode") int httpCode, String msgKey) {
		_errorCode = errorCode;
		_httpCode = httpCode;
		_msgKey = msgKey;
	}
	
	@Override
	public int getErrorCode() { return _errorCode; }
	
	public int getHttpCode() { return _httpCode; }

	@JsonIgnore
	public String getMessageKey() { return _msgKey; }
	
	@Override
	public String getMessage() {
		return rBundle.getString(_msgKey);
	}

	public String getMessage(String arg1) {
		return MessageFormat.format(rBundle.getString(_msgKey), arg1);
	}
	

}
