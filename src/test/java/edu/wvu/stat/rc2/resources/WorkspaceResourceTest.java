package edu.wvu.stat.rc2.resources;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.GenericType;

import org.junit.ClassRule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import edu.wvu.stat.rc2.persistence.RCWorkspace;
import io.dropwizard.testing.junit.ResourceTestRule;

public class WorkspaceResourceTest extends BaseResourceTest {

	@ClassRule
	public static final ResourceTestRule resources = ResourceTestRule.builder()
		.addResource(new WorkspaceResource(new DBI(dbfactory.getDataSource()), user))
		.build();

	@Test
	public void testWorkspaceList() {
//		String x = resources.client().target("/workspaces").request().get(String.class);
//		System.err.println(x);
		Map<String,Object> responseDict = resources.client().target("/workspaces").request()
				.get(new GenericType<Map<String,Object>>(){});;
		assertEquals(responseDict.get("status"), 0);
		@SuppressWarnings("unchecked")
		List<RCWorkspace> wspaces = (List<RCWorkspace>) responseDict.get("workspaces");
		assertEquals(wspaces.size(), 2);
	}

}
