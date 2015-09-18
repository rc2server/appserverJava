package edu.wvu.stat.rc2;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;

import edu.wvu.stat.rc2.persistence.RCFile;
import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.persistence.RCWorkspace;

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

	public static RCWorkspace mockWorkspace() {
		RCWorkspace wspace = RCWorkspace.create(1, 1, 1, "test ws");
		ArrayList<RCFile> files = new ArrayList<RCFile>();
		Date now = new Date();
		files.add(RCFile.create(1, 1, "test.R", 1, now, now, 1024));
		files.add(RCFile.create(2, 1, "data.csv", 1, now, now, 1024));
		wspace.setFiles(files);
		return wspace;
	}
	
}
