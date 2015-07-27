package edu.wvu.stat.rc2.resources;

import static org.junit.Assert.*;

import org.junit.ClassRule;
import org.junit.Test;

import edu.wvu.stat.rc2.persistence.PGDataSourceFactory;
import edu.wvu.stat.rc2.persistence.RCUser;
import io.dropwizard.testing.junit.ResourceTestRule;

public class UserResourceTest {
	private static final PGDataSourceFactory factory = new PGDataSourceFactory();

	@ClassRule
	public static final ResourceTestRule resources = ResourceTestRule.builder()
		.addResource(new UserResource(factory))
		.build();
	
	@Test
	public void testGetUser() {
		RCUser user = resources.client().target("/users/1").request().get(RCUser.class);
		assert(user.getId() == 1);
		assertEquals("Mark", user.getFirstName());
		assertEquals("Lilback", user.getLastName());
		assertEquals("mark@lilback.com", user.getEmail());
		assertEquals("mlilback", user.getLogin());
		assert(user.isAdmin());
		assert(user.isEnabled());
	}

}
