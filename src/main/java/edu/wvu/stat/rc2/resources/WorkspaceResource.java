package edu.wvu.stat.rc2.resources;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.validator.constraints.NotEmpty;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
		RCUser user = getUser();
		RCWorkspace.Queries dao = _dbi.onDemand(RCWorkspace.Queries.class);
		List<RCWorkspace> wspaces = dao.ownedByUser(user.getId());
		if (null == wspaces) {
			log.warn(String.format("no workspaces found for user %s", user.getLogin()));
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		return wspaces;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public RCWorkspace createWorkspace(@Valid WorkspacePostInput input) {
		RCWorkspace.Queries dao = _dbi.onDemand(RCWorkspace.Queries.class);
		RCWorkspace ws = dao.findByName(input.getName());
		if (ws != null)
			throwCustomRestError(RCRestError.DuplicateName, "workspace");
		RCUser user = getUser();
		int wsid = dao.createWorkspace(input.getName(), user.getId());
		return dao.findById(wsid);
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public RCWorkspace updateWorkspace(@Valid WorkspacePutInput input) {
		RCWorkspace.Queries dao = _dbi.onDemand(RCWorkspace.Queries.class);
		int upcount = dao.updateWorkspace(input.getId(), input.getName());
		if (upcount !=1)
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		return dao.findById(input.getId());
	}
	
	@DELETE
	@Path("{id}")
	public Response deleteWorkspace(@PathParam("id") int id) {
		RCWorkspace.Queries dao = _dbi.onDemand(RCWorkspace.Queries.class);
		dao.deleteWorkspace(id);
		return Response.status(Response.Status.NO_CONTENT).build();
	}
	
	public static class WorkspacePostInput {
		private final String _name;
		
		@JsonCreator
		public WorkspacePostInput(@JsonProperty("name") String name) {
			_name = name;
		}
		
		public @NotEmpty String getName() { return _name; }
	}
	
	public static class WorkspacePutInput {
		private final int _id;
		private final String _name;
		
		@JsonCreator
		public WorkspacePutInput(@JsonProperty("id") int id, @JsonProperty("name") String name) {
			_id = id;
			_name = name;
		}
		
		public @JsonProperty @Min(1) int getId() { return _id; }
		public @JsonProperty @NotEmpty String getName() { return _name; }
	}
}
