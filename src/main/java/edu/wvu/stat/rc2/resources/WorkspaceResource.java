package edu.wvu.stat.rc2.resources;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.wvu.stat.rc2.persistence.RCSessionImage;
import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.persistence.RCWorkspace;
import edu.wvu.stat.rc2.persistence.Rc2DAO;

@Path("/workspaces")
@Produces(MediaType.APPLICATION_JSON)
public class WorkspaceResource extends BaseResource {
	final static Logger log= LoggerFactory.getLogger("rc2.WorkspaceResource");
	
	public WorkspaceResource() {
		super();
		
	}

	public WorkspaceResource(Rc2DAO dao, RCUser user) {
		super(dao, user);
	}

	@Path("{id}/files")
	public FileResource fileResource(@PathParam("id") int wspaceId) {
		try {
			RCWorkspace wspace = getDAO().findWorkspaceById(wspaceId);
			checkWorkspacePermissions(wspace);
			FileResource rsrc = new FileResource(getDAO(), getUser(), wspace);
			return rsrc;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	// MARK: SessionImage access
	
	@Path("{id}/images/{iid}")
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getImage(@PathParam("id") int wspaceId, @PathParam("iid") int imageId) {
		RCWorkspace wspace = getDAO().findWorkspaceById(wspaceId);
		checkWorkspacePermissions(wspace);
		RCSessionImage img = getDAO().findImageById(imageId);
		if (img.getWorkspaceId() != wspaceId) {
			throw new WebApplicationException(Response.Status.FORBIDDEN);
		}
		Response rsp = Response.ok()
						.entity(img.getImageData())
						//last mod = today - 7 days
						.lastModified(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)))
						.build();
		return rsp;
		
	}
	
	// MARK: actual workspace methods
	
	@GET
	public List<RCWorkspace> workspaces() {
		RCUser user = getUser();
		List<RCWorkspace> wspaces = getDAO().getWorkspaceDao().ownedByUserIncludingFiles(user.getId());
		if (null == wspaces) {
			log.warn(String.format("no workspaces found for user %s", user.getLogin()));
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		return wspaces;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public RCWorkspace createWorkspace(@Valid WorkspacePostInput input) {
		RCWorkspace ws = getDAO().getWorkspaceDao().findByName(input.getName());
		if (ws != null)
			throwCustomRestError(RCRestError.DuplicateName, "workspace");
		RCUser user = getUser();
		int wsid = getDAO().getWorkspaceDao().createWorkspace(input.getName(), user.getId());
		return getDAO().findWorkspaceById(wsid);
	}
	
	@PUT
	@Path("{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public RCWorkspace updateWorkspace(@Valid WorkspacePutInput input) {
		RCWorkspace existing = getDAO().getWorkspaceDao().findByName(input.getName());
		if (existing != null)
			throwCustomRestError(RCRestError.DuplicateName, "workspace");
		RCWorkspace wspace = getDAO().findWorkspaceById(input.getId());
		checkWorkspacePermissions(wspace);
		int upcount = getDAO().getWorkspaceDao().updateWorkspace(input.getId(), input.getName());
		if (upcount !=1)
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		return getDAO().findWorkspaceById(input.getId());
	}
	
	@DELETE
	@Path("{id}")
	public Response deleteWorkspace(@PathParam("id") int id) {
		RCWorkspace wspace = getDAO().findWorkspaceById(id);
		checkWorkspacePermissions(wspace);
		getDAO().getWorkspaceDao().deleteWorkspace(id);
		return Response.status(Response.Status.NO_CONTENT).build();
	}
	
	
	private void checkWorkspacePermissions(RCWorkspace wspace) throws WebApplicationException {
		if (null == wspace)
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		if (!getPermChecker().canAccessWorkspace(wspace))
			throw new WebApplicationException(Response.Status.FORBIDDEN);
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
