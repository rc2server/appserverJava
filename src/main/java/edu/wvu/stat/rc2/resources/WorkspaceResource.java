package edu.wvu.stat.rc2.resources;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.wvu.stat.rc2.jdbi.TransactionHandleWrapper;
import edu.wvu.stat.rc2.persistence.RCFile;
import edu.wvu.stat.rc2.persistence.RCFileQueries;
import edu.wvu.stat.rc2.persistence.RCProject;
import edu.wvu.stat.rc2.persistence.RCSessionImage;
import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.persistence.RCWorkspace;
import edu.wvu.stat.rc2.persistence.RCWorkspaceQueries;
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
			FileResource rsrc = new FileResource(getDAO(), getUser(), wspace, getConfig().getFileDownloadDelay());
			return rsrc;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	// MARK: SessionImage access
	
	@Path("{id}/images/{iid}")
	@GET
	@Produces("image/png")
	public Response getImage(@PathParam("id") int wspaceId, @PathParam("iid") int imageId) {
		RCWorkspace wspace = getDAO().findWorkspaceById(wspaceId);
		checkWorkspacePermissions(wspace);
		RCSessionImage img = getDAO().findImageById(imageId);
		if (null == img) {
			log.warn("failed to find image " + imageId);
			return Response.status(Response.Status.NOT_FOUND).build();
		}
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
	public Response createWorkspace(@Valid WorkspacePostInput input) {
		RCWorkspace ws = getDAO().getWorkspaceDao().findByNameAndProject(input.getName(), input.getProjectId());
		if (ws != null)
			throwCustomRestError(RCRestError.DuplicateName, "workspace");
		RCUser user = getUser();
		int wsid = getDAO().getWorkspaceDao().createWorkspace(input.getName(), input.getProjectId(), user.getId());
		return Response.status(Response.Status.CREATED)
				.entity(getDAO().getWorkspaceDao().findByIdIncludingFiles(wsid))
				.build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public Response createWorkspaceWithFiles(File inputFile, @Context HttpHeaders headers) 
	{
		String wspaceName = null;
		int projectId = 0;
		int wsid = -1;
		ZipFile zfile = null;
		try {
			String pidstr = headers.getHeaderString("Rc2-ProjectId");
			projectId = Integer.parseInt(pidstr);
			wspaceName = headers.getHeaderString("Rc2-WorkspaceName");
			zfile = new ZipFile(inputFile);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		if (wspaceName == null || zfile == null)
			return Response.status(Response.Status.BAD_REQUEST).build();
		RCProject project = getDAO().getProjectDao().findById(projectId);
		if (project == null)
			return Response.status(Response.Status.BAD_REQUEST).build();
		RCWorkspace ws = getDAO().getWorkspaceDao().findByNameAndProject(wspaceName, projectId);
		if (ws != null)
			throwCustomRestError(RCRestError.DuplicateName, "workspace");
		RCUser user = getUser();
		try (TransactionHandleWrapper trans = getDAO().createTransactionWrapper())
		{
			RCFileQueries fileDao = trans.addDao(RCFileQueries.class);
			RCWorkspaceQueries wsDao = trans.addDao(RCWorkspaceQueries.class);
			wsid = wsDao.createWorkspace(wspaceName, projectId, user.getId());
			//read zip file, creating file objects
			for (ZipEntry entry : Collections.list(zfile.entries())) {
				try {
					RCFile aFile = fileDao.createFileWithStream(wsid, entry.getName(), zfile.getInputStream(entry));
					if (aFile == null) {
						trans.rollback();
						return Response.status(Response.Status.BAD_REQUEST).build();
					}
				} catch (Exception ie) {
					log.error("failed to inserted file", ie);
					trans.rollback();
					return Response.status(Response.Status.BAD_REQUEST).build();
				}
			}
		}
		return Response.status(Response.Status.CREATED)
				.entity(getDAO().getWorkspaceDao().findByIdIncludingFiles(wsid))
				.build();
	}
	
	@PUT
	@Path("{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public RCWorkspace updateWorkspace(@Valid WorkspacePutInput input) {
		RCWorkspace existing = getDAO().getWorkspaceDao().findById(input.getId());
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
		private final int _projectId;
		
		@JsonCreator
		public WorkspacePostInput(@JsonProperty("name") String name, @JsonProperty("projectId") int projectId) 
		{
			_name = name;
			_projectId = projectId;
		}
		
		public @NotEmpty String getName() { return _name; }
		public @Min(1) int getProjectId() { return _projectId; }
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
