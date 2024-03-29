package edu.wvu.stat.rc2;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import edu.wvu.stat.rc2.persistence.RCFile;
import edu.wvu.stat.rc2.persistence.RCFileQueries;
import edu.wvu.stat.rc2.persistence.RCProjectQueries;
import edu.wvu.stat.rc2.persistence.RCSessionRecord;
import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.persistence.RCWorkspace;
import edu.wvu.stat.rc2.persistence.RCWorkspaceQueries;
import edu.wvu.stat.rc2.persistence.Rc2DAO;
import edu.wvu.stat.rc2.persistence.Rc2DataSourceFactory;
import edu.wvu.stat.rc2.rworker.RWorker;

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
		RCWorkspace wspace = RCWorkspace.create(1, 1,  1, "xdfsdfsdfds", "test ws");
		ArrayList<RCFile> files = new ArrayList<RCFile>();
		Date now = new Date();
		files.add(RCFile.create(1, 1, "test.R", 1, now, now, 1024));
		files.add(RCFile.create(2, 1, "data.csv", 1, now, now, 1024));
		wspace.setFiles(files);
		return wspace;
	}
	
	public static class RCMockDBObjects {
		public Rc2DataSourceFactory factory;
		public Rc2DAO dao;
		public RCFileQueries fdao;
		public Connection dbConnection;
		RCMockDBObjects(Rc2DataSourceFactory fact, Rc2DAO dao, RCFileQueries queries) {
			this.factory = fact;
			this.dao = dao;
			this.fdao = queries;
			this.dbConnection = dao.getDBI().open().getConnection();
		}
	}
	
	public static Rc2DataSourceFactory mockDBFactoryForSession(RCWorkspace wspace) {
		return mockDBObjectsForSession(wspace).factory;
	}

	public static RCMockDBObjects mockDBObjectsForSession(RCWorkspace wspace) {
		Rc2DataSourceFactory dbfactory = mock(Rc2DataSourceFactory.class);
		Rc2DAO dao = mock(Rc2DAO.class);
		when(dbfactory.createDAO()).thenReturn(dao);
		when(dao.findWorkspaceById(1)).thenReturn(wspace);
		RCSessionRecord.Queries srecDao = mock(RCSessionRecord.Queries.class);
		when(srecDao.createSessionRecord(1)).thenReturn(1);
		when(srecDao.closeSessionRecord(1)).thenReturn(1);
		DBI sessionDbi = mock(DBI.class);
		when(sessionDbi.onDemand(RCSessionRecord.Queries.class)).thenReturn(srecDao);
		Handle handle = mock(Handle.class);
		when(handle.getConnection()).thenReturn(mock(Connection.class));
		when(sessionDbi.open()).thenReturn(handle);
		when(dao.getDBI()).thenReturn(sessionDbi);
		RCFileQueries fdao = mock(RCFileQueries.class);
		when(fdao.filesForWorkspaceId(wspace.getId())).thenReturn(wspace.getFiles());
		when(fdao.findById(wspace.getFiles().get(0).getId())).thenReturn(wspace.getFiles().get(0));
		when(dao.getFileDao()).thenReturn(fdao);
		RCWorkspaceQueries wsDao = mock(RCWorkspaceQueries.class);
		when(dao.getWorkspaceDao()).thenReturn(wsDao);
		when(wsDao.findById(wspace.getId())).thenReturn(wspace);
		when(wsDao.findByIdIncludingFiles(wspace.getId())).thenReturn(wspace);
		RCProjectQueries pdao = mock(RCProjectQueries.class);
		when(dao.getProjectDao()).thenReturn(pdao);
		return new RCMockDBObjects(dbfactory, dao, fdao);
	}

	public static class MockSocketFactory extends RWorker.SocketFactory {
		InputStream mockIn = mock(InputStream.class);
		OutputStream mockOut = mock(OutputStream.class);
		Socket mockSocket = mock(Socket.class);
		public MockSocketFactory() throws IOException {
			when(mockSocket.getInputStream()).thenReturn(mockIn);
			when(mockSocket.getOutputStream()).thenReturn(mockOut);
		}
		public Socket createSocket(String host, int port) throws UnknownHostException, IOException {
			return mockSocket;
		}
	}

}
