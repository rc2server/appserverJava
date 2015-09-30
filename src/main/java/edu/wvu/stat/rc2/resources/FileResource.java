package edu.wvu.stat.rc2.resources;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wvu.stat.rc2.jdbi.TransactionHandleWrapper;
import edu.wvu.stat.rc2.persistence.RCFile;
import edu.wvu.stat.rc2.persistence.RCFileQueries;
import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.persistence.RCWorkspace;

@Produces(MediaType.APPLICATION_JSON)
public class FileResource {
	final static Logger log= LoggerFactory.getLogger("rc2.FileResource");
	final RCWorkspace _wspace;
	final RCUser _user;
	final DBI _dbi;

	public FileResource(DBI dbi, RCUser user, RCWorkspace wspace) {
		_dbi = dbi;
		_user = user;
		_wspace = wspace;
	}

	@Path("{fid : \\d+}")
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public final Response getFile(@Context Request request, @PathParam("fid") int fileId) {
		RCFileQueries fdao = _dbi.onDemand(RCFileQueries.class);
		RCFile file = fdao.findById(fileId);
		if (null == file)
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		EntityTag etag = new EntityTag(file.getETag());
		ResponseBuilder builder = request.evaluatePreconditions(etag);
		if (builder != null)
			return builder.build();
		byte[] fileData = fdao.fileDataById(fileId);
		Response rsp = Response.ok().entity(fileData).tag(etag).lastModified(file.getLastModified()).build();
		return rsp;
	}
	
	
	@GET
	public List<RCFile> getFiles(@PathParam("id") int wspaceId) {
		RCFileQueries fdao = _dbi.onDemand(RCFileQueries.class);
		return fdao.filesForWorkspaceId(wspaceId);
	}
	

	@Path("upload")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(
			@FormDataParam("file") InputStream inStream, 
			@FormDataParam("file") FormDataContentDisposition fileDetails) 
	{
		try (TransactionHandleWrapper trans = new TransactionHandleWrapper(_dbi)) {
			RCFileQueries fileDao = trans.addDao(RCFileQueries.class);
			RCFile file = fileDao.createFileWithStream(_wspace.getId(), fileDetails.getFileName(), inStream);
			if (null == file) //should really be impossible
				throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		
			return Response.status(Response.Status.CREATED).entity(file).build();
		}
	}
	
}
