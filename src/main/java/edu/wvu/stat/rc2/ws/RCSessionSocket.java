package edu.wvu.stat.rc2.ws;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.tweak.VoidHandleCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import edu.wvu.stat.rc2.persistence.RCUser;

@WebSocket
public final class RCSessionSocket {
	static final Logger log = LoggerFactory.getLogger("rc2.RCSessionSocket");
	private static final AtomicInteger sNextId = new AtomicInteger(0);

	private Session _outbound;
	private final ObjectWriter _objWriter;
	private final int _socketId;
	private final long _connectTime;
	private final String _client;
	private String _displayName;
	private RCUser _user;
	private Delegate _delegate;
	
	public RCSessionSocket(ServletUpgradeRequest upRequest, Delegate delegate, ObjectMapper mapper, RCUser user) {
		_delegate = delegate;
		_socketId = sNextId.getAndIncrement();
		_connectTime = System.currentTimeMillis();
		_user = user;

		_objWriter = mapper.writer();

		HttpServletRequest request = upRequest.getHttpServletRequest();
		String client = request.getParameter("client");
		if (null == client || (!client.equals("ios") && !client.equals("osx")))
			client = "unknown";
		_client = client;
		_displayName = user.getLogin();
	}

	public int getSocketId() { return _socketId; }
	public int getUserId() { return _user.getId(); }
	public String getDisplayName() { return _displayName; }
	public String getClient() { return _client; }
	public long getConnectTime() { return _connectTime; }
	public Delegate getDelegate() { return _delegate; }

	@OnWebSocketConnect
	public void onOpen(Session out) {
		_outbound = out;
		_delegate.websocketOpened(this);
		Map<String,Object> jo = new HashMap<String,Object>();
		try {
			jo.put("msg", "userid");
			jo.put("userid", _user.getId());
			jo.put("socketId",  getSocketId());
			jo.put("session", _delegate.getSessionDescriptionForWebsocket(this));
			sendMessage(_objWriter.writeValueAsString(jo));
		} catch (Exception e) {
			log.warn("exception parsing connect dict", e);
		}
	}

	@OnWebSocketClose
	public void onClose(int closeCode, String message) {
		_outbound=null;
		_delegate.websocketClosed(this);
		_delegate=null;
	}

	@OnWebSocketMessage
	public void onBinaryMessage(byte[] data, int offset, int length) {
		_delegate.processWebsocketBinaryMessage(this, data, offset, length);
	}
	
	@OnWebSocketMessage
	public void onMessage(String data) {
		log.info("got message:" + data);
		_delegate.processWebsocketMessage(this, data);
	}

	public void sendMessage(String msg) {
		if (null != _outbound)
			_outbound.getRemote().sendStringByFuture(msg);
	}

	public void sendBinaryMessage(byte[] data, int offset, int length) {
		if (null != _outbound) {
			ByteBuffer buf = ByteBuffer.wrap(data, offset, length);
			_outbound.getRemote().sendBytesByFuture(buf);
		}
	}

	
	@SuppressWarnings("unused")
	private void updateUserFromDatabase() {
		_delegate.websocketUseDatabaseHandle(new VoidHandleCallback() {
			protected void execute(Handle h) {
				RCUser.Queries queries = h.attach(RCUser.Queries.class);
				_user = queries.findById(_user.getId());
			}});
	}

	public Map<String,Object> jsonDescription() {
		Map<String,Object> aUser = new HashMap<String,Object>();
		aUser.put("sid", getSocketId());
		aUser.put("userId", getUserId());
		aUser.put("displayName", getDisplayName());
		aUser.put("client", getClient());
		aUser.put("connectTime", Long.toString(getConnectTime()));
		return aUser;
	}

	interface Delegate {
		public int getWorkspaceId();
		public void websocketUseDatabaseHandle(HandleCallback<Void> callback);
		public Map<String,Object> getSessionDescriptionForWebsocket(RCSessionSocket socket);
		public void websocketOpened(RCSessionSocket socket);
		public void websocketClosed(RCSessionSocket socket);
		public void processWebsocketMessage(RCSessionSocket socket, String msg);
		public void processWebsocketBinaryMessage(RCSessionSocket socket, byte[] data, int offset, int length);
	}
}
