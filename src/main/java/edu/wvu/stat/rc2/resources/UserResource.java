package edu.wvu.stat.rc2.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.persistence.Rc2DAO;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource extends BaseResource {

	public UserResource() {
		super();
		
	}

	public UserResource(Rc2DAO dao, RCUser user) {
		super(dao, user);
		
	}

	@Path("users/{userid}")
	@GET
	public RCUser getUser(@PathParam("userid") String userid) {
		RCUser user = _dao.findUserById(Integer.parseInt(userid));
		if (null == user) {
			throw new WebApplicationException(404);
		}
		return user;
	}
}
