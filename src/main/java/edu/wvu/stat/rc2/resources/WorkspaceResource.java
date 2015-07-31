package edu.wvu.stat.rc2.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.persistence.RCWorkspace;

@Path("/workspaces")
@Produces(MediaType.APPLICATION_JSON)
public class WorkspaceResource extends BaseResource {
	final static Logger log= LoggerFactory.getLogger(WorkspaceResource.class);
	
	public WorkspaceResource() {
		super();
		
	}

	public WorkspaceResource(DBI dbi, RCUser user) {
		super(dbi, user);
		
	}

	@GET
	public List<RCWorkspace> workspaces() {
		RCWorkspace.Queries dao = _dbi.onDemand(RCWorkspace.Queries.class);
		List<RCWorkspace> wspaces = dao.ownedByUser(_user.getId());
		if (null == wspaces) {
			log.warn(String.format("no workspaces found for user %s", _user.getLogin()));
			throw new WebApplicationException(404);
		}
		return wspaces;
	}
}
