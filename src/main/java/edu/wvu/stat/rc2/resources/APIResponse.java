package edu.wvu.stat.rc2.resources;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

public final class APIResponse implements JsonSerializable {
	private final int _status;
	private final HashMap<String,Object> _map;
	
	
	
	public APIResponse() {
		this(0);
	}

	public APIResponse(int status) {
		_status = status;
		_map = new HashMap<String,Object>();
	}
	
	
	public APIResponse(int status, String key, Object value) {
		this(status);
		_map.put(key, value);
	}
	
	public int getStatus() { return _status; }
	
	public Object get(String key) {
		return _map.get(key);
	}
	
	//to get a list of objects without type safety warnings from compiler
	public <T> List<T> getList(String key, Class<T> type) {
		List<?> genlist = (List<?>) _map.get(key);
		List<T> list = genlist.stream().map(e -> (type.cast(e))).collect(Collectors.toList());
		return list;
	}
	
	public void put(String key, Object val) {
		_map.put(key, val);
	}

	@Override
	public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException {
		jgen.writeStartObject();
		jgen.writeNumberField("status", _status);
		for(Map.Entry<String, Object> entry : _map.entrySet()) {
			jgen.writeObjectField(entry.getKey(), entry.getValue());
		};
		jgen.writeEndObject();
	}

	@Override
	public void serializeWithType(JsonGenerator jgen, SerializerProvider provider, TypeSerializer serializer) throws IOException {
		jgen.writeNumberField("status", _status);
		for(Map.Entry<String, Object> entry : _map.entrySet()) {
			jgen.writeObjectField(entry.getKey(), entry.getValue());
		};
	}

}
