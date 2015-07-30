package edu.wvu.stat.rc2;

import javax.inject.Singleton;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.skife.jdbi.v2.DBI;

import edu.wvu.stat.rc2.persistence.PGDataSourceFactory;
import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.resources.UserResource;
import edu.wvu.stat.rc2.rs.LoggedInUserFactory;
import edu.wvu.stat.rc2.rs.Rc2DBInject;
import edu.wvu.stat.rc2.rs.Rc2DBInjectResolver;
import edu.wvu.stat.rc2.rs.UserInject;
import edu.wvu.stat.rc2.rs.UserInjectResolver;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class Rc2Application extends Application<Rc2AppConfiguration> {
	private final PGDataSourceFactory dbfactory = new PGDataSourceFactory();
	
	
	public static void main(String[] args) throws Exception {
		new Rc2Application().run(args);
	}
	
	@Override
	public String getName() {
		return "rc2";
	}
	
	@Override
	public void initialize(Bootstrap<Rc2AppConfiguration> bootstrap) {
		
	}
	
	@Override
	public void run(Rc2AppConfiguration config, Environment env) {
		env.jersey().register(UserResource.class);

		env.jersey().register(new AbstractBinder() {
			@Override
			protected void configure() {
				bindFactory(LoggedInUserFactory.class).to(RCUser.class).in(RequestScoped.class);
				bind(UserInjectResolver.class)
					.to(new TypeLiteral<InjectionResolver<UserInject>>(){})
					.in(Singleton.class);
				bindFactory(DBIFactory.class).to(DBI.class).in(RequestScoped.class);
				bind(Rc2DBInjectResolver.class)
					.to(new TypeLiteral<InjectionResolver<Rc2DBInject>>(){})
					.in(Singleton.class);
			}
		});

	}

	class DBIFactory implements Factory<DBI> {
		@Override
		public void dispose(DBI arg0) {
		}

		@Override
		public DBI provide() {
			return new DBI(dbfactory.getDataSource());
		}
		
	}
}
