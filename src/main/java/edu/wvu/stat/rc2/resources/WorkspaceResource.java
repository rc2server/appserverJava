package edu.wvu.stat.rc2.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.wvu.stat.rc2.persistence.RCWorkspace;

@Path("/workspaces")
@Produces(MediaType.APPLICATION_JSON)
public class WorkspaceResource extends BaseResource {

	@GET
	public List<RCWorkspace> workspaces() {
		return null;
	}
}
