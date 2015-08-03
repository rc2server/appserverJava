package edu.wvu.stat.rc2;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

public interface RCError extends JsonSerializable {
	int getErrorCode();
	String getMessage();

	@Override
	default void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException {
		jgen.writeStartObject();
		jgen.writeNumberField("errorCode", this.getErrorCode());
		String msg = this.getMessage();
		jgen.writeStringField("message", msg);
		jgen.writeEndObject();
	}

	@Override
	default void serializeWithType(JsonGenerator jgen, SerializerProvider provider, TypeSerializer serializer) throws IOException {
		jgen.writeNumberField("errorCode", this.getErrorCode());
		jgen.writeStringField("message", this.getMessage());
	}
}
