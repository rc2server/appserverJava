package edu.wvu.stat.rc2.persistence;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
//import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import edu.wvu.stat.rc2.RCIntegrationTest;

@Category(RCIntegrationTest.class)
public class DBNotificationTest {
	volatile String _lastMessage;
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testNotification() throws Exception {
		_lastMessage = "";
		final String channel = "rc2file";
		CountDownLatch latch = new CountDownLatch(1);
		Rc2DataSourceFactory.NotificationListener listener = (String channelName, String message) -> {
			_lastMessage = message;
			latch.countDown();
		};
		Rc2DataSourceFactory factory = new Rc2DataSourceFactory();
		factory.addNotificationListener(channel , listener);
		DBI dbi = factory.createDBI();
		dbi.withHandle((Handle h) -> {
			h.execute("select pg_notify('" + channel  + "', '11')");
			return null;
		});
		latch.await(5, TimeUnit.SECONDS);
		assertThat(_lastMessage, is("11"));
	}

}
