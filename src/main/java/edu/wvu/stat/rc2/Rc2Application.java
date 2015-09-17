package edu.wvu.stat.rc2;

import java.util.EnumSet;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Singleton;
import javax.servlet.DispatcherType;

import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.skife.jdbi.v2.DBI;

import com.fasterxml.jackson.databind.SerializationFeature;

import edu.wvu.stat.rc2.persistence.Rc2DataSourceFactory;
import edu.wvu.stat.rc2.resources.LoginResource;
import edu.wvu.stat.rc2.resources.UserResource;
import edu.wvu.stat.rc2.resources.WorkspaceResource;
import edu.wvu.stat.rc2.rs.Rc2DBInject;
import edu.wvu.stat.rc2.rs.Rc2DBInjectResolver;
import edu.wvu.stat.rc2.server.HashPasswordCommand;
import edu.wvu.stat.rc2.ws.RCSessionCache;
import edu.wvu.stat.rc2.ws.RCSessionServlet;
import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class Rc2Application extends Application<Rc2AppConfiguration> {
	private static final Rc2DataSourceFactory dbfactory = new Rc2DataSourceFactory();
	
	
	public static void main(String[] args) throws Exception {
		new Rc2Application().run(args);
	}
	
	private ScheduledExecutorService _execService;
	private RCSessionCache _sessionCache;
	
	@Override
	public String getName() {
		return "rc2";
	}
	
	@Override
	public void initialize(Bootstrap<Rc2AppConfiguration> bootstrap) {
		bootstrap.addCommand(new HashPasswordCommand());
		bootstrap.addBundle(new MultiPartBundle());
	}
	
	@Override
	public void run(Rc2AppConfiguration config, Environment env) {
		_sessionCache = new RCSessionCache(dbfactory, env.getObjectMapper());
		_execService = env.lifecycle().scheduledExecutorService("rc2-exec", true).build();
		_sessionCache.scheduleCleanupTask(_execService);
		env.lifecycle().manage(_sessionCache);
		
		if (config.getPrettyPrint())
			env.getObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		env.servlets().addFilter("Rc2AuthServletFilter", new Rc2AuthServletFilter(dbfactory))
			.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
		env.jersey().register(UserResource.class);
		env.jersey().register(WorkspaceResource.class);
//		env.jersey().register(FileResource.class);
		env.jersey().register(LoginResource.class);
		
		ServletHolder h = new ServletHolder(new RCSessionServlet(_sessionCache));
		env.getApplicationContext().addServlet(h, "/ws/*");

		env.jersey().register(new AbstractBinder() {
			@Override
			protected void configure() {
				bindFactory(DBIFactory.class).to(DBI.class).in(RequestScoped.class);
				bind(Rc2DBInjectResolver.class)
					.to(new TypeLiteral<InjectionResolver<Rc2DBInject>>(){})
					.in(Singleton.class);
			}
		});

		env.healthChecks().register("database", new DatabaseHealthCheck(dbfactory));
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
