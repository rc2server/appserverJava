package edu.wvu.stat.rc2;

import java.util.EnumSet;
import java.util.concurrent.ScheduledExecutorService;

import javax.servlet.DispatcherType;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;

import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ServerProperties;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.fasterxml.jackson.databind.SerializationFeature;

import edu.wvu.stat.rc2.persistence.Rc2DAO;
import edu.wvu.stat.rc2.persistence.Rc2DataSourceFactory;
import edu.wvu.stat.rc2.resources.LoginResource;
import edu.wvu.stat.rc2.resources.UserResource;
import edu.wvu.stat.rc2.resources.WorkspaceResource;
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
	private Rc2DAO _dao;
	
	@Override
	public String getName() {
		return "rc2";
	}
	
	@Override
	public void initialize(Bootstrap<Rc2AppConfiguration> bootstrap) {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		bootstrap.addCommand(new HashPasswordCommand());
		bootstrap.addBundle(new MultiPartBundle());
	}
	
	@Override
	public void run(Rc2AppConfiguration config, Environment env) {
		_sessionCache = new RCSessionCache(dbfactory, env.getObjectMapper());
		_execService = env.lifecycle().scheduledExecutorService("rc2-exec", true).build();
		_sessionCache.scheduleCleanupTask(_execService);
		env.lifecycle().manage(_sessionCache);
		if (config.getEnableTracing()) {
			env.jersey().getResourceConfig().property(ServerProperties.TRACING, "ON_DEMAND");
			env.jersey().getResourceConfig().property(ServerProperties.TRACING_THRESHOLD, "TRACE");
		}
		
		if (config.getPrettyPrint())
			env.getObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		env.servlets().addFilter("Rc2AuthServletFilter", new Rc2AuthServletFilter(dbfactory))
			.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
		env.jersey().register(UserResource.class);
		env.jersey().register(WorkspaceResource.class);
		env.jersey().register(LoginResource.class);
		
		ServletHolder h = new ServletHolder(new RCSessionServlet(_sessionCache));
		env.getApplicationContext().addServlet(h, "/ws/*");

		env.jersey().register(new DAOInjectFilter());

		env.healthChecks().register("database", new DatabaseHealthCheck(dbfactory));
		System.err.println("run complete");
	}

	//uses double check idom to make we get a valid dao 
	public Rc2DAO getDAO() {
		Rc2DAO dao = _dao;
		if (null == dao) {
			synchronized(this) {
				if (null == _dao) {
					_dao = dbfactory.createDAO();
				}
				dao = _dao;
			}
		}
		return dao;
	}
	
	@PreMatching
	class DAOInjectFilter implements ContainerRequestFilter {
		public void filter(ContainerRequestContext ctx) {
			ctx.setProperty("rc2.dao", getDAO());
		}
	}
}
