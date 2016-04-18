package edu.wvu.stat.rc2.ws;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import edu.wvu.stat.rc2.Rc2CommonMocks;
import edu.wvu.stat.rc2.config.SessionConfigImpl;
import edu.wvu.stat.rc2.persistence.RCFile;
import edu.wvu.stat.rc2.persistence.RCWorkspace;
import edu.wvu.stat.rc2.persistence.Rc2DataSourceFactory;
import edu.wvu.stat.rc2.rworker.RWorker;
import edu.wvu.stat.rc2.ws.response.FileChangedResponse;
import edu.wvu.stat.rc2.ws.response.FileChangedResponse.ChangeType;

public class SessionFileNotificationTest {
	RCWorkspace _wspace;
	Rc2DataSourceFactory _dbfactory;
	
	@Before
	public void setUp() throws Exception {
		_wspace = Rc2CommonMocks.mockWorkspace();
		_dbfactory = Rc2CommonMocks.mockDBFactoryForSession(_wspace);
	}

	@Test
	public void testUpdateNotification() throws Exception {
		RCFile file = _wspace.getFiles().get(0);
		final String note = "u" + file.getId() + "/0/0";
		FileChangedResponse  rsp = generateNotification(note);
		assertThat(rsp.getType(), is(ChangeType.Update));
		assertThat(rsp.getFile().getId(), is(file.getId()));
	}

	@Test
	public void testInsertNotification() throws Exception {
		RCFile file = _wspace.getFiles().get(0);
		final String note = "i" + file.getId() + "/0/0";
		FileChangedResponse  rsp = generateNotification(note);
		assertThat(rsp.getType(), is(ChangeType.Insert));
		assertThat(rsp.getFile().getId(), is(file.getId()));
	}

	@Test
	public void testDeleteNotification() throws Exception {
		RCFile file = _wspace.getFiles().get(0);
		final String note = "d" + file.getId();
		FileChangedResponse  rsp = generateNotification(note);
		assertThat(rsp.getType(), is(ChangeType.Delete));
		assertThat(rsp.getFile().getId(), is(file.getId()));
	}

	FileChangedResponse generateNotification(String note) throws Exception { 
		RCSession session = new RCSession(_dbfactory, null, new SessionConfigImpl(), 1, new RWorker(new Rc2CommonMocks.MockSocketFactory(), null));
		RCSessionSocket socket = mock(RCSessionSocket.class);
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		session.websocketOpened(socket);
		session.handleNotification("rcfile", note);
		Mockito.verify(socket).sendMessage(captor.capture());
		try {
			FileChangedResponse rsp = session.getObjectMapper().readerFor(FileChangedResponse.class).readValue(captor.getValue());
			return rsp;
		} catch (Exception e) {
			e.printStackTrace();
			fail("failed to decode file changed response");
		}
		return null;
	}
}
