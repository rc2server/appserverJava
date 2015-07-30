package edu.wvu.stat.rc2.rs;

import javax.inject.Inject;
import javax.inject.Named;

import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceHandle;

import edu.wvu.stat.rc2.persistence.RCUser;

public class UserInjectResolver implements InjectionResolver<UserInject> {

	@Inject
	@Named(InjectionResolver.SYSTEM_RESOLVER_NAME)
	InjectionResolver<Inject> systemInjectionResolver;
	
	@Override
	public boolean isConstructorParameterIndicator() {
		return false;
	}

	@Override
	public boolean isMethodParameterIndicator() {
		return false;
	}

	@Override
	public Object resolve(Injectee injectee, ServiceHandle<?> handle) {
		if (RCUser.class == injectee.getRequiredType()) {
			return systemInjectionResolver.resolve(injectee, handle);
		}
		return null;
	}

	

}
