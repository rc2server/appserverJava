package edu.wvu.stat.rc2.persistence;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;

import com.google.common.io.Files;

import edu.wvu.stat.rc2.UnitTestDBConfig;

public class RCSessionImageTest {
	static final Rc2DataSourceFactory dbfactory = new Rc2DataSourceFactory(new UnitTestDBConfig());
	private Rc2DAO _dao;
	private int _imgId, _sessionId;
	
	@Before
	public void setUp() throws Exception {
		_dao = dbfactory.createDAO();
	}
	
	@After
	public void tearDown() {
		_dao.getDBI().withHandle(new HandleCallback<Void>() {
			@Override
			public Void withHandle(Handle h) throws Exception {
				h.update("delete from sessionimage where id = ?", _imgId);
				h.update("delete from sessionrecord where id = ?", _sessionId);
				return null;
			}
		});
	}
	
	@Test
	public void testCreateImage() throws Exception {
		URL fileUrl = this.getClass().getResource("/image.png");
		assertNotNull(fileUrl);
		File rawFile = new File(fileUrl.getFile());
		assert(rawFile.exists());
		byte[] imgData = Files.toByteArray(rawFile);

		RCSessionRecord.Queries srecDao = _dao.getDBI().onDemand(RCSessionRecord.Queries.class);
		_sessionId = srecDao.createSessionRecord(1);

		_dao.getDBI().withHandle(new HandleCallback<Void>() {
			@Override
			public Void withHandle(Handle h) throws Exception {
				List<Map<String,Object>> results = h.select(
					"insert into sessionimage (sessionid,batchid,name,imgdata) values(?,101,'image.png',?) returning id", 
					_sessionId, imgData);
				_imgId = (int) results.get(0).get("id"); 
				return null;
			}
		});
		assertThat(_imgId, is(greaterThan(0)));
		List<RCSessionImage> img = _dao.getSessionImageDao().findByBatchId(101, _sessionId);
		assertThat(img.size(), is(1));
		assertThat(img.get(0).getName(), is("image.png"));
		assertThat(img.get(0).getImageData(), is(imgData));
	}

}
