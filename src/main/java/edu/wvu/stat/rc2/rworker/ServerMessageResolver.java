package edu.wvu.stat.rc2.rworker;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;

import edu.wvu.stat.rc2.rworker.message.BaseMessage;

public class ServerMessageResolver extends TypeIdResolverBase {
	public final String MESSAGE_PREFIX = "edu.wvu.stat.rc2.rworker.message";
	
	@Override
	public Id getMechanism() {
		return Id.CUSTOM;
	}

	@Override
	public String idFromValue(Object obj) {
		return idFromValueAndType(obj, obj.getClass());
	}

	@Override
	public String idFromValueAndType(Object obj, Class<?> clazz) {
		String name = clazz.getName();
		if (name.startsWith(MESSAGE_PREFIX)) {
			return name.substring(MESSAGE_PREFIX.length() + 1);
		}
		throw new IllegalStateException("class " + clazz + " is not in package " + MESSAGE_PREFIX);
	}

	@Override
	public JavaType typeFromId(DatabindContext context, String type) {
		JavaType baseType = null;
		//following is not used, not sure why
		String typeClass=null;
		switch(type) {
			case "results":
				typeClass = "ResultsMessage";
				break;
			case "error":
				typeClass = "ErrorMessage";
				break;
			case "showoutput":
				typeClass = "ShowOutputMessage";
				break;
			case "execComplete":
				typeClass = "ExecCompleteMessage";
				break;
			case "variableupdate":
				typeClass = "VariableUpdateMessage";
				break;
			case "variablevalue":
				typeClass = "VariableValueMessage";
				break;
			case "help":
				typeClass = "HelpMessage";
				break;
			default:
				throw new IllegalStateException("cannot handle message of type '" + type + "'");
		}
		Class<?> clazz;
		String clazzName = MESSAGE_PREFIX + "." + typeClass;
		try {
			clazz = TypeFactory.defaultInstance().findClass(clazzName);
			baseType = context.constructType(BaseMessage.class);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("cannot find class '" + clazzName + "'");
		}
		return TypeFactory.defaultInstance().constructSpecializedType(baseType, clazz);
	}

}
