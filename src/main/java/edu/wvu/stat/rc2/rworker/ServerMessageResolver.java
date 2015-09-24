package edu.wvu.stat.rc2.rworker;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;

import edu.wvu.stat.rc2.rworker.message.BaseMessage;

public class ServerMessageResolver extends TypeIdResolverBase {
	public enum Messages {
		EXEC_COMPLETE_MSG ("execComplete"),
		RESULTS_MSG ("results"),
		ERROR_MSG  ("error"),
		SHOW_OUTPUT_MSG ("showoutput"),
		VAR_UPDATE_MSG ("variableupdate"),
		VAR_VALUE_MSG ("variablevalue"),
		HELP_MSG ("help");
		
		public final String jsonValue;
		private Messages(String s) { jsonValue = s; }
		public static Messages valueWithJsonValue(String aValue) {
			for (Messages msg : Messages.values()) {
				if (msg.jsonValue.equals(aValue))
					return msg;
			}
			return null;
		}
	}
	
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
		switch(Messages.valueWithJsonValue(type)) {
			case RESULTS_MSG:
				typeClass = "ResultsMessage";
				break;
			case ERROR_MSG:
				typeClass = "ErrorMessage";
				break;
			case SHOW_OUTPUT_MSG:
				typeClass = "ShowOutputMessage";
				break;
			case EXEC_COMPLETE_MSG:
				typeClass = "ExecCompleteMessage";
				break;
			case VAR_UPDATE_MSG:
				typeClass = "VariableUpdateMessage";
				break;
			case VAR_VALUE_MSG:
				typeClass = "VariableValueMessage";
				break;
			case HELP_MSG:
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
