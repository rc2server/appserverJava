package edu.wvu.stat.rc2;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.wvu.stat.rc2.persistence.RCUser;

public class Rc2CommonMocks {

	public static RCUser mockTestUser() {
		RCUser user = mock(RCUser.class);
		when(user.getEmail()).thenReturn("cornholio@stat.wvu.edu");
		when(user.getFirstName()).thenReturn("Great");
		when(user.getLastName()).thenReturn("Cornholio");
		when(user.getLogin()).thenReturn("cornholio");
		when(user.getId()).thenReturn(1);
		when(user.isAdmin()).thenReturn(false);
		when(user.isEnabled()).thenReturn(true);
		return user;
	}

}
