package edu.wvu.stat.rc2.resources;


import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.impossibl.postgres.utils.guava.ByteStreams;

import edu.wvu.stat.rc2.Rc2CommonMocks;
import edu.wvu.stat.rc2.Rc2CommonMocks.RCMockDBObjects;
import edu.wvu.stat.rc2.jdbi.TransactionHandleWrapper;
import edu.wvu.stat.rc2.UnitTestDBConfig;
import edu.wvu.stat.rc2.persistence.RCFileQueries;
import edu.wvu.stat.rc2.persistence.RCProject;
import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.persistence.RCWorkspace;
import edu.wvu.stat.rc2.persistence.RCWorkspaceQueries;
import edu.wvu.stat.rc2.persistence.Rc2DataSourceFactory;
import edu.wvu.stat.rc2.resources.WorkspaceResource.WorkspacePostInput;

import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

public class WorkspaceResourceTests {
	static final Rc2DataSourceFactory dbfactory = new Rc2DataSourceFactory(new UnitTestDBConfig());
	RCMockDBObjects dbObjs;
	RCWorkspace wspace;
	RCUser user;
	WorkspaceResource resource;
	
	@Before
	public void setUp() throws Exception {
		wspace = Rc2CommonMocks.mockWorkspace();
		dbObjs = Rc2CommonMocks.mockDBObjectsForSession(wspace);
		user = Rc2CommonMocks.mockTestUser();
		resource = new WorkspaceResource(dbObjs.dao, user);
	}

	@Test
	public void testCreateWorkspace() {
		RCWorkspace newWspace = RCWorkspace.create(2, 1,  1, "fdsfd", "test ws");
		RCWorkspaceQueries wsDao = dbObjs.dao.getWorkspaceDao();
		when(wsDao.findByIdIncludingFiles(2)).thenReturn(newWspace);
		when(wsDao.findByNameAndProject(anyString(), anyInt())).thenReturn(null);
		when(wsDao.createWorkspace("test ws", 1, user.getId())).thenReturn(2);;
		WorkspacePostInput input = new WorkspacePostInput("test ws", 1);
		Response rsp = resource.createWorkspace(input);
		assertThat(rsp.getStatus(), is(201));
		Object entity = rsp.getEntity();
		assertThat(entity, is(newWspace));
	}

	@Test
	public void testCreateWorkspaceDuplicate() {
		RCWorkspace newWspace = RCWorkspace.create(2, 1,  1, "fdsfd", "test ws");
		RCWorkspaceQueries wsDao = dbObjs.dao.getWorkspaceDao();
		when(wsDao.findByNameAndProject(anyString(), anyInt())).thenReturn(newWspace);
		WorkspacePostInput input = new WorkspacePostInput("test ws", 1);
		try {
			Response rsp = resource.createWorkspace(input);
			assertThat(rsp.getStatus(), is(422));
		} catch (javax.ws.rs.WebApplicationException e) {
			assertThat(e.getResponse().getStatus(), is(422));
		}
	}

	@Test
	public void testCreateWorkspaceWithFiles() throws Exception {
		//create a zip file with fake data when can later compare against
		String file1text = "file1\nmore\n";
		byte[] file1 = file1text.getBytes(StandardCharsets.UTF_8);
		byte[] file2 = new byte[43]; 
		new Random().nextBytes(file2);
		File zFile = null;
		try {
			zFile = File.createTempFile("ztest", "zip");
			ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zFile));
			ZipEntry e1 = new ZipEntry("file1");
			ZipEntry e2 = new ZipEntry("file2");
			zout.putNextEntry(e1);
			zout.write(file1);
			zout.closeEntry();
			zout.putNextEntry(e2);
			zout.write(file2);
			zout.close();
		} catch (Exception e) {
			fail("exception setting up test");
			throw new java.lang.Error(e);
		}
		
		//setup additional mocks
		RCWorkspaceQueries wsDao = dbObjs.dao.getWorkspaceDao();
		when(wsDao.findByNameAndProject(anyString(), anyInt())).thenReturn(null);
		when(wsDao.createWorkspace("foo", 1, user.getId())).thenReturn(2);
		RCProject project = RCProject.create(1, 1, user.getId(), "proj1");
		when(dbObjs.dao.getProjectDao().findById(1)).thenReturn(project);
		TransactionHandleWrapper twrapper = mock(TransactionHandleWrapper.class);
		when(twrapper.addDao(RCFileQueries.class)).thenReturn(dbObjs.fdao);
		when(twrapper.addDao(RCWorkspaceQueries.class)).thenReturn(wsDao);
		when(dbObjs.dao.createTransactionWrapper()).thenReturn(twrapper);

		//capture arguments to createFileWithStream
		ArgumentCaptor<InputStream> isCaptor = ArgumentCaptor.forClass(InputStream.class);
		ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
		when(dbObjs.dao.getFileDao()
			.createFileWithStream(idCaptor.capture(), nameCaptor.capture(), isCaptor.capture()))
			.thenReturn(wspace.getFiles().get(0));
		
		//mock required headers
		HttpHeaders headers = mock(HttpHeaders.class);
		when(headers.getHeaderString("Rc2-ProjectId")).thenReturn("1");
		when(headers.getHeaderString("Rc2-WorkspaceName")).thenReturn("foo");
		
		Response rsp = resource.createWorkspaceWithFiles(zFile, headers);
		assertThat(rsp.getStatus(), is(201));
		assertThat(idCaptor.getAllValues().size(), is(2));
		List<String> names = nameCaptor.getAllValues();
		assertThat(names.size(), is(2));
		assertThat(names.get(0), is("file1"));
		assertThat(names.get(1), is("file2"));
		List<InputStream> streams = isCaptor.getAllValues();
		assertThat(ByteStreams.toByteArray(streams.get(0)), is(file1));
		assertThat(ByteStreams.toByteArray(streams.get(1)), is(file2));
	}

	@Test
	public void testUpdateWorkspace() {
	}

	@Test
	public void testDeleteWorkspace() {
	}

}
