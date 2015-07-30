package edu.wvu.stat.rc2.rs;

import javax.inject.Inject;
import javax.inject.Named;

import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceHandle;
import org.skife.jdbi.v2.DBI;

public class Rc2DBInjectResolver implements InjectionResolver<Rc2DBInject> {

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
		if (DBI.class == injectee.getRequiredType())
			return systemInjectionResolver.resolve(injectee, handle);
			
		return null;
	}

}
