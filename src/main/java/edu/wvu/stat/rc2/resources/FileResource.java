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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

//import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
//import org.glassfish.jersey.media.multipart.FormDataParam;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wvu.stat.rc2.jdbi.TransactionHandleWrapper;
import edu.wvu.stat.rc2.persistence.RCFile;
import edu.wvu.stat.rc2.persistence.RCFileQueries;
import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.persistence.RCWorkspace;
import edu.wvu.stat.rc2.persistence.Rc2DAO;

@Produces(MediaType.APPLICATION_JSON)
public class FileResource {
	final static Logger log= LoggerFactory.getLogger("rc2.FileResource");
	final RCWorkspace _wspace;
	final RCUser _user;
	final DBI _dbi;
	final long _downloadDelay;

	public FileResource(Rc2DAO dao, RCUser user, RCWorkspace wspace) {
		this(dao, user, wspace, 0);
	}
	
	public FileResource(Rc2DAO dao, RCUser user, RCWorkspace wspace, long downloadDelay) {
		_dbi = dao.getDBI();
		_user = user;
		_wspace = wspace;
		_downloadDelay = downloadDelay;
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
		if (builder != null) {
			log.info("using builder for file " + fileId + "etag:" + etag);
			return builder.build();
		}
		byte[] fileData = fdao.fileDataById(fileId);
		if (_downloadDelay > 0) {
			log.info("delaying download by " + _downloadDelay);
			try {
				Thread.sleep(_downloadDelay);
			} catch (InterruptedException ie) {
			}
		}
		Response rsp = Response.ok().entity(fileData).tag(etag).lastModified(file.getLastModified()).build();
		log.info("returning file:" + rsp);
		return rsp;
	}
	
	
	@GET
	public List<RCFile> getFiles(@PathParam("id") int wspaceId) {
		RCFileQueries fdao = _dbi.onDemand(RCFileQueries.class);
		return fdao.filesForWorkspaceId(wspaceId);
	}
	

	@Path("upload")
	@POST
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public Response uploadStream(InputStream stream, @Context HttpHeaders headers)
	{
		log.info("handling uploadStream: " + headers);
		try (TransactionHandleWrapper trans = new TransactionHandleWrapper(_dbi)) {
			RCFileQueries fileDao = trans.addDao(RCFileQueries.class);
			RCFile file = fileDao.createFileWithStream(_wspace.getId(), headers.getHeaderString("Rc2-Filename"), stream);
			if (null == file) { //should really be impossible
				log.warn("uploadFile without file specified:" + headers);
				throw new WebApplicationException("file parameter missing", Response.Status.INTERNAL_SERVER_ERROR);
			}
		
			return Response.status(Response.Status.CREATED).entity(file).build();
		}
	}

//	@Path("upload")
//	@POST
//	@Consumes(MediaType.MULTIPART_FORM_DATA)
//	public Response uploadFile(
//			@Context HttpHeaders headers, 
//			@FormDataParam("file") InputStream inStream, 
//			@FormDataParam("file") FormDataContentDisposition fileDetails) 
//	{
//		log.info("handling uploadFile");
//		try (TransactionHandleWrapper trans = new TransactionHandleWrapper(_dbi)) {
//			RCFileQueries fileDao = trans.addDao(RCFileQueries.class);
//			RCFile file = fileDao.createFileWithStream(_wspace.getId(), fileDetails.getFileName(), inStream);
//			if (null == file) { //should really be impossible
//				log.warn("uploadFile without file specified:" + headers);
//				throw new WebApplicationException("file parameter missing", Response.Status.INTERNAL_SERVER_ERROR);
//			}
//		
//			return Response.status(Response.Status.CREATED).entity(file).build();
//		}
//	}
	
}
