package edu.wvu.stat.rc2.persistence;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
//import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import edu.wvu.stat.rc2.UnitTestDBConfig;
import edu.wvu.stat.rc2.jdbi.TransactionHandleWrapper;

public class ProjectTest {
	static final Rc2DataSourceFactory dbfactory = new Rc2DataSourceFactory(new UnitTestDBConfig());
	private DBI _dbi;

	@Before
	public void setUp() throws Exception {
		_dbi = dbfactory.createDBI();
	}
	@Test
	public void testfetchProjects() {
		try (TransactionHandleWrapper trans = new TransactionHandleWrapper(_dbi)) {
			RCProjectQueries dao = trans.addDao(RCProjectQueries.class);
			List<RCProject> projects = dao.ownedByUserIncludingWorkspacesAndFiles(1);
			assertThat(projects.size(), is(1));
			assertThat(projects.get(0).getId(), is(1));
			List<RCWorkspace> wspaces = projects.get(0).getWorkspaces();
			assertThat(wspaces.size(), is(2));
			assertThat(wspaces.get(0).getId(), is(1));
			assertThat(wspaces.get(0).getFiles().size(), is(2));
			assertThat(wspaces.get(0).getFiles().get(0).getId(), is(1));
			assertThat(wspaces.get(0).getFiles().get(0).getName(), is("sample.R"));
		}
	}

}
