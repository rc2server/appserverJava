package edu.wvu.stat.rc2.resources;

import static org.junit.Assert.*;

import org.junit.ClassRule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import edu.wvu.stat.rc2.persistence.RCUser;
import io.dropwizard.testing.junit.ResourceTestRule;

public class UserResourceTest extends BaseResourceTest {
	
	@ClassRule
	public static final ResourceTestRule resources = ResourceTestRule.builder()
		.addResource(new UserResource(new DBI(dbfactory.getDataSource())))
		.build();
	
	@Test
	public void testGetUser() {
		RCUser user = resources.client().target("/users/1").request().get(RCUser.class);
		assertEquals(1, user.getId());
		assertEquals("Great", user.getFirstName());
		assertEquals("Cornholio", user.getLastName());
		assertEquals("cornholio@stat.wvu.edu", user.getEmail());
		assertEquals("test", user.getLogin());
		assertFalse(user.isAdmin());
		assertTrue(user.isEnabled());
	}

}
