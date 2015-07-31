package edu.wvu.stat.rc2.resources;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

import javax.ws.rs.core.GenericType;

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
		assertThat(list, hasSize(2));
		assertThat(list, hasItems(hasProperty("name", is("foofy")), hasProperty("name", is("thrice"))));
	}

	@Test
	public void testWorkspaceListJson() throws JSONException {
		String x = resources.client().target("/workspaces").request().get(String.class);
		JSONAssert.assertEquals("[{id:1,name:\"foofy\"},{id:2,name:\"thrice\"}]", x, false);
	}

}
