package edu.wvu.stat.rc2.resources;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.ClassRule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skyscreamer.jsonassert.JSONAssert;

import edu.wvu.stat.rc2.persistence.RCWorkspace;
import io.dropwizard.testing.junit.ResourceTestRule;

public class WorkspaceResourceTest extends BaseResourceTest {

	@ClassRule
	public static final ResourceTestRule resources = ResourceTestRule.builder()
		.addResource(new WorkspaceResource(new DBI(dbfactory.getDataSource()), user))
		.build();

	@SuppressWarnings("unchecked")
	@Test
	public void testWorkspaceList() {
		List<RCWorkspace> list = resources.client().target("/workspaces").request()
				.get(new GenericType<List<RCWorkspace>>(){});
		assertThat(list.size(), greaterThanOrEqualTo(2));
		assertThat(list, hasItems(hasProperty("name", is("foofy")), hasProperty("name", is("thrice"))));
	}

	@Test
	public void testWorkspaceListJson() throws JSONException {
		//have to parse a bit as jsonassert can only exactly match arrays and there might be more than 2 workspaces
		String x = resources.client().target("/workspaces").request().get(String.class);
		JSONArray wspaces = new JSONArray(x);
		JSONAssert.assertEquals("{id:1,name:\"foofy\"}", wspaces.getJSONObject(0), false);
		JSONAssert.assertEquals("{id:2,name:\"thrice\"}]", wspaces.getJSONObject(1), false);
	}

	@Test
	public void testCreateEditDeleteWorkspace() {
		WebTarget target = resources.client().target("/workspaces");
		WorkspaceResource.WorkspacePostInput input = new WorkspaceResource.WorkspacePostInput("testws");
		RCWorkspace ws = target
							.request(MediaType.APPLICATION_JSON_TYPE)
							.post(Entity.entity(input, MediaType.APPLICATION_JSON), RCWorkspace.class);
		assertNotNull(ws);
		assertThat(ws.getName(), is("testws"));
		assertThat(ws.getId(), greaterThan(0));
		try {
			//try an update
			WorkspaceResource.WorkspacePutInput update = new WorkspaceResource.WorkspacePutInput(ws.getId(), "doofus123");
			RCWorkspace modWs = target
									.request(MediaType.APPLICATION_JSON)
									.put(Entity.entity(update, MediaType.APPLICATION_JSON), RCWorkspace.class);
			assertThat(modWs.getId(), is(ws.getId()));
			assertThat(modWs.getName(), is(update.getName()));
		} finally {
			//delete workspace we just created
			Response rsp = resources.client().target("/workspaces/" + ws.getId()).request().delete();
			assertThat(rsp.getStatus(), is(Response.Status.NO_CONTENT.getStatusCode()));
		}
	}
	
	@Test(expected=WebApplicationException.class)
	public void testUpdateNonexistentWorkspace() {
		WebTarget target = resources.client().target("/workspaces");
		//need to use valid id, else get processing exception
		WorkspaceResource.WorkspacePutInput update = new WorkspaceResource.WorkspacePutInput(200000, "doofus123");
		target
			.request(MediaType.APPLICATION_JSON)
			.put(Entity.entity(update, MediaType.APPLICATION_JSON), RCWorkspace.class);
	}
}
