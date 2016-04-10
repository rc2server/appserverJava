package edu.wvu.stat.rc2.persistence;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import edu.wvu.stat.rc2.UnitTestDBConfig;
import edu.wvu.stat.rc2.jdbi.TransactionHandleWrapper;

public class RCFileTest {
	static final Rc2DataSourceFactory dbfactory = new Rc2DataSourceFactory(new UnitTestDBConfig());
	private DBI _dbi;

	@Before
	public void setUp() throws Exception {
		_dbi = dbfactory.createDBI();
	}
	
	@Test
	public void testCreateFile() {
		try (TransactionHandleWrapper trans = new TransactionHandleWrapper(_dbi)) {
			RCFileQueries dao = trans.addDao(RCFileQueries.class);
			URL fileUrl = this.getClass().getResource("/sample.R");
			assertNotNull(fileUrl);
			File rawFile = new File(fileUrl.getFile());
			assert(rawFile.exists());
			RCFile file = dao.createFile(1, rawFile.getName(), rawFile);
			try {
				assertThat(file.getFileSize(), is((int)rawFile.length()));
				assertThat(file.getName(), is(rawFile.getName()));
//				dao.createFile(1, "foo.R", rawFile);
			} finally {
				dao.deleteFile(file.getId());
			}
		}
	}

	@Test
	public void testCreateFileFromStream() throws IOException {
		try (TransactionHandleWrapper trans = new TransactionHandleWrapper(_dbi)) {
			RCFileQueries dao = trans.addDao(RCFileQueries.class);
			URL fileUrl = this.getClass().getResource("/sample.R");
			assertNotNull(fileUrl);
			File rawFile = new File(fileUrl.getFile()); 
			assert(rawFile.exists());
			RCFile file = dao.createFileWithStream(1, rawFile.getName(), new FileInputStream(rawFile));
			try {
				assertThat(file.getFileSize(), is((int)rawFile.length()));
				assertThat(file.getName(), is(rawFile.getName()));
			} finally {
				dao.deleteFile(file.getId());
			}
		}
	}
}
