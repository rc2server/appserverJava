package edu.wvu.stat.rc2.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.skife.jdbi.v2.DBI;

import edu.wvu.stat.rc2.persistence.RCUser;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource extends BaseResource {

	public UserResource() {
		super();
		
	}

	public UserResource(DBI dbi, RCUser user) {
		super(dbi, user);
		
	}

	@Path("users/{userid}")
	@GET
	public RCUser getUser(@PathParam("userid") String userid) {
		RCUser.Queries dao = _dbi.onDemand(RCUser.Queries.class);
		RCUser user = dao.findById(Integer.parseInt(userid));
		if (null == user) {
			throw new WebApplicationException(404);
		}
		return user;
	}
}
