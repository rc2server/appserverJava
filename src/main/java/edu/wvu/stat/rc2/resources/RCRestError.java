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
	private int errorCode, httpCode;
	private String msgKey;
	
	@JsonCreator
	RCRestError(@JsonProperty("errorCode") int errorCode, @JsonProperty("httpCode") int httpCode, String msgKey) {
		this.errorCode = errorCode;
		this.httpCode = httpCode;
		this.msgKey = msgKey;
	}
	
	@Override
	public int getErrorCode() { return this.errorCode; }
	
	public int getHttpCode() { return this.httpCode; }

	@JsonIgnore
	public String getMessageKey() { return this.msgKey; }
	
	@Override
	public String getMessage() {
		return rBundle.getString(this.msgKey);
	}

	public String getMessage(String arg1) {
		return MessageFormat.format(rBundle.getString(this.msgKey), arg1);
	}
	

}
