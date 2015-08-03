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

	private RCRestError error;
	private String details;
	
	public RCCustomError(RCRestError err, String details) {
		this.error = err;
		this.details = details;
	}
	
	public RCRestError getError() { return this.error; }
	public String getDetails() { return this.details; }
	
	
	@Override
	public int getErrorCode() { return this.error.getErrorCode(); }
	
	@Override
	public String getMessage() { return this.error.getMessage(this.getDetails()); }
	
	@Override
	public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException {
		jgen.writeStartObject();
		jgen.writeNumberField("errorCode", this.error.getErrorCode());
		String msg = this.error.getMessage(this.details);
		jgen.writeStringField("message", msg);
		jgen.writeEndObject();
	}

	@Override
	public void serializeWithType(JsonGenerator jgen, SerializerProvider provider, TypeSerializer serializer) throws IOException {
		jgen.writeNumberField("errorCode", this.error.getErrorCode());
		jgen.writeStringField("message", this.error.getMessage(this.details));
	}

}
