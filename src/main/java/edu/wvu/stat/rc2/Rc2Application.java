package edu.wvu.stat.rc2;

import javax.inject.Singleton;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.skife.jdbi.v2.DBI;

import edu.wvu.stat.rc2.persistence.PGDataSourceFactory;
import edu.wvu.stat.rc2.resources.LoginResource;
import edu.wvu.stat.rc2.resources.UserResource;
import edu.wvu.stat.rc2.resources.WorkspaceResource;
import edu.wvu.stat.rc2.rs.Rc2DBInject;
import edu.wvu.stat.rc2.rs.Rc2DBInjectResolver;
import edu.wvu.stat.rc2.server.HashPasswordCommand;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class Rc2Application extends Application<Rc2AppConfiguration> {
	private static final PGDataSourceFactory dbfactory = new PGDataSourceFactory();
	
	
	public static void main(String[] args) throws Exception {
		new Rc2Application().run(args);
	}
	
	@Override
	public String getName() {
		return "rc2";
	}
	
	@Override
	public void initialize(Bootstrap<Rc2AppConfiguration> bootstrap) {
		bootstrap.addCommand(new HashPasswordCommand());
	}
	
	@Override
	public void run(Rc2AppConfiguration config, Environment env) {
		env.jersey().register(UserResource.class);
		env.jersey().register(WorkspaceResource.class);
		env.jersey().register(LoginResource.class);

		env.jersey().register(new AbstractBinder() {
			@Override
			protected void configure() {
				bindFactory(DBIFactory.class).to(DBI.class).in(RequestScoped.class);
				bind(Rc2DBInjectResolver.class)
					.to(new TypeLiteral<InjectionResolver<Rc2DBInject>>(){})
					.in(Singleton.class);
			}
		});

		env.jersey().register(new Rc2AuthFilter(dbfactory));
	}

	static class DBIFactory implements Factory<DBI> {
		@Override
		public void dispose(DBI arg0) {
		}

		@Override
		public DBI provide() {
			return dbfactory.createDBI();
		}
		
	}
}
