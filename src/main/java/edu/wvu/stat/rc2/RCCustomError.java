package edu.wvu.stat.rc2;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import edu.wvu.stat.rc2.resources.RCRestError;

/**
	Custom implementation of RCError that allows a detail string in the localized message to be inserted.
	
	@author mlilback
*/
public class RCCustomError implements RCError {

	private RCRestError _error;
	private String _details;
	
	public RCCustomError(RCRestError err, String details) {
		_error = err;
		_details = details;
	}
	
	public RCRestError getError() { return _error; }
	public String getDetails() { return _details; }
	
	
	@Override
	public int getErrorCode() { return _error.getErrorCode(); }
	
	@Override
	public String getMessage() { return _error.getMessage(this.getDetails()); }
	
	@Override
	public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException {
		jgen.writeStartObject();
		jgen.writeNumberField("errorCode", _error.getErrorCode());
		String msg = _error.getMessage(_details);
		jgen.writeStringField("message", msg);
		jgen.writeEndObject();
	}

	@Override
	public void serializeWithType(JsonGenerator jgen, SerializerProvider provider, TypeSerializer serializer) throws IOException {
		jgen.writeNumberField("errorCode", _error.getErrorCode());
		jgen.writeStringField("message", _error.getMessage(this._details));
	}

}
