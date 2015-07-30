package edu.wvu.stat.rc2.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.skife.jdbi.v2.DBI;

import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.rs.Rc2DBInject;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource extends BaseResource {

	@Rc2DBInject DBI _dbi;
	
	public UserResource() {}
	
	/** constructor for unit tests to bypass injection */
	public UserResource(DBI dbi) {
		_dbi = dbi;
	}
	
	@Path("users/{userid}")
	@GET
	public RCUser getUser(@PathParam("userid") String userid) {
		RCUser.UserQueries dao = _dbi.onDemand(RCUser.UserQueries.class);
		RCUser user = dao.findById(Integer.parseInt(userid));
		if (null == user) {
			throw new WebApplicationException(404);
		}
		return user;
	}
}
