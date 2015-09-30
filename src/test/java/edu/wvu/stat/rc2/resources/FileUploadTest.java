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
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wvu.stat.rc2.RCIntegrationTest;
import edu.wvu.stat.rc2.Rc2CommonMocks;
import edu.wvu.stat.rc2.persistence.RCFile;
import edu.wvu.stat.rc2.persistence.RCFileQueries;
import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.persistence.Rc2DAO;
import edu.wvu.stat.rc2.persistence.Rc2DataSourceFactory;

@Category(RCIntegrationTest.class)
public class FileUploadTest extends JerseyTest {
	static final Rc2DataSourceFactory dbfactory = new Rc2DataSourceFactory();
	static final Rc2DAO dao = dbfactory.createDAO();
	
	static final RCUser user = Rc2CommonMocks.mockTestUser();

	public class UploadTestConfig extends ResourceConfig {
		public UploadTestConfig() {
			register(MultiPartFeature.class);
			register(new WorkspaceResource(dao.getDBI(), user));
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
	public void testListFiles() {
		//TODO: implement
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
//		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		RCFile file = mapper.readValue(json, RCFile.class);
		assertThat(file.getName(), is("foo.R"));
		//delete the file we created
		RCFileQueries fdao = dao.getDBI().onDemand(RCFileQueries.class);
		fdao.deleteFile(file.getId());
	}
}
