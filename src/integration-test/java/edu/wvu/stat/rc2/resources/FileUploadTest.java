package edu.wvu.stat.rc2.resources;

import static org.junit.Assert.*;

import static org.hamcrest.Matchers.*;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.json.JSONArray;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wvu.stat.rc2.Rc2CommonMocks;
import edu.wvu.stat.rc2.persistence.RCFile;
import edu.wvu.stat.rc2.persistence.RCFileQueries;
import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.persistence.Rc2DAO;
import edu.wvu.stat.rc2.persistence.Rc2DataSourceFactory;

public class FileUploadTest extends JerseyTest {
	static final Rc2DataSourceFactory dbfactory = new Rc2DataSourceFactory();
	static final Rc2DAO dao = dbfactory.createDAO();
	
	static final RCUser user = Rc2CommonMocks.mockTestUser();

	public class UploadTestConfig extends ResourceConfig {
		public UploadTestConfig() {
			register(MultiPartFeature.class);
			register(new WorkspaceResource(dao, user));
		}
	}

	@BeforeClass
	public static void initLogger() {
		if (!SLF4JBridgeHandler.isInstalled()) {
			SLF4JBridgeHandler.removeHandlersForRootLogger();
			SLF4JBridgeHandler.install();
		}
	}
	
	@Override
	protected Application configure() {
		return new UploadTestConfig();
	}
	
	@Override
	protected void configureClient(ClientConfig config) {
		config.register(MultiPartFeature.class);
	}
	
	@Test
	public void testListFiles() throws Exception {
		WebTarget target = this.getClient().target("/workspaces/1/files");
		Response rsp = target.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
		assertThat(rsp.getStatusInfo(), is(Response.Status.OK));
		String json = rsp.readEntity(String.class);
		
		JSONArray obj = new JSONArray(json);
		JSONAssert.assertEquals("{id:1,name:\"sample.R\"}", obj.getJSONObject(0), false);
		JSONAssert.assertEquals("{id:2,name:\"foo.R\"}]", obj.getJSONObject(1), false);
		
		//test getting a file's contents
		target = this.getClient().target(String.format("/workspaces/1/files/%s", obj.getJSONObject(0).getInt("id")));
		rsp = target.request().accept(MediaType.APPLICATION_OCTET_STREAM).get();
		String str = rsp.readEntity(String.class);
		assertThat(str.length(), is(obj.getJSONObject(0).getInt("fileSize")));
	}
	
	@Test 
	public void testGetFile() {
		//TODO: implement
	}
	
	@Test
	public void testFileUpload() throws Exception {
		WebTarget target = this.getClient().target("/workspaces/1/files/upload");
		final FormDataMultiPart mp = new FormDataMultiPart();
		final String value = "test file\n contents\n";
		final FormDataContentDisposition  dispo = FormDataContentDisposition
				.name("file")
				.fileName("foo.R")
				.size(value.getBytes().length)
				.build();
		final FormDataBodyPart bodyPart = new FormDataBodyPart(dispo, value);
		mp.bodyPart(bodyPart);
		
		Response rsp = target.request(MediaType.MULTIPART_FORM_DATA_TYPE)
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(mp, mp.getMediaType()));
		assertThat(rsp.getStatusInfo(), is(Response.Status.CREATED));
		String json = rsp.readEntity(String.class);
		ObjectMapper mapper = new ObjectMapper();
		RCFile file = mapper.readValue(json, RCFile.class);
		assertThat(file.getName(), is("foo.R"));
		//delete the file we created
		RCFileQueries fdao = dao.getDBI().onDemand(RCFileQueries.class);
		fdao.deleteFile(file.getId());
	}
}
