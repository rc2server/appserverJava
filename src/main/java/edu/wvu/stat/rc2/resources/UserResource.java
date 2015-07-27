package edu.wvu.stat.rc2.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.skife.jdbi.v2.DBI;

import edu.wvu.stat.rc2.persistence.PGDataSourceFactory;
import edu.wvu.stat.rc2.persistence.RCUser;

@Path("/users/{userid}")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource extends BaseResource {

	public UserResource(PGDataSourceFactory factory) {
		super(factory);
	}
	
	@GET
	public RCUser getUser(@PathParam("userid") String userid) {
		DBI dbi = new DBI(_dsFactory.getDataSource());
		RCUser.UserQueries dao = dbi.onDemand(RCUser.UserQueries.class);
		RCUser user = dao.findById(Integer.parseInt(userid));
		if (null == user) {
			throw new WebApplicationException(404);
		}
		return user;
	}
}
