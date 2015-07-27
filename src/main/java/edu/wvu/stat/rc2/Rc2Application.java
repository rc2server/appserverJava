package edu.wvu.stat.rc2;

import edu.wvu.stat.rc2.persistence.PGDataSourceFactory;
import edu.wvu.stat.rc2.resources.UserResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class Rc2Application extends Application<Rc2AppConfiguration> {
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
		PGDataSourceFactory factory = new PGDataSourceFactory();
		env.jersey().register(new UserResource(factory));
	}
}
