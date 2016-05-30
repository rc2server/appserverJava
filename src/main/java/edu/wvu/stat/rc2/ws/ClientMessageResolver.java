package edu.wvu.stat.rc2.ws;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;

import edu.wvu.stat.rc2.ws.request.BaseRequest;

public class ClientMessageResolver extends TypeIdResolverBase {
	public enum Messages {
		EXEC_MSG ("execute"),
		USERLIST_MSG ("userList"),
		KEEPALIVE_MSG ("keepAlive"),
		WATCHVARS_MSG ("watchVariables"),
		GETVAR_MSG ("getVariable"),
		HELP_MSG ("help"),
		SAVE_MSG ("save"),
		FILE_MSG ("fileop");
		
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
	
	public final String MESSAGE_PREFIX = "edu.wvu.stat.rc2.ws.request";

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
			case EXEC_MSG:
				typeClass = "ExecuteRequest";
				break;
			case USERLIST_MSG:
				typeClass = "UserListRequest";
				break;
			case HELP_MSG:
				typeClass = "HelpRequest";
				break;
			case KEEPALIVE_MSG:
				typeClass = "KeepAliveRequest";
				break;
			case WATCHVARS_MSG:
				typeClass = "WatchVariablesRequest";
				break;
			case GETVAR_MSG:
				typeClass = "GetVariableRequest";
				break;
			case SAVE_MSG:
				typeClass = "SaveRequest";
				break;
			case FILE_MSG:
				typeClass = "FileRequest"; 
				break;
			default:
				throw new IllegalStateException("cannot handle client request of type '" + type + "'");
		}
		Class<?> clazz;
		String clazzName = MESSAGE_PREFIX + "." + typeClass;
		try {
			clazz = TypeFactory.defaultInstance().findClass(clazzName);
			baseType = context.constructType(BaseRequest.class);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("cannot find class '" + clazzName + "'");
		}
		return TypeFactory.defaultInstance().constructSpecializedType(baseType, clazz);
	}
}
