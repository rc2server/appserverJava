package edu.wvu.stat.rc2.rworker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wvu.stat.rc2.persistence.RCSessionImage;
import edu.wvu.stat.rc2.persistence.Rc2DAO;
import edu.wvu.stat.rc2.rworker.response.*;
import edu.wvu.stat.rc2.ws.response.*;

public class RWorker implements Runnable {
	static final Logger log = LoggerFactory.getLogger("rc2.RWorker");
	private static final int MAGIC_IDENT = 0x21;
	private static final int OUT_QUEUE_SIZE = 20;

	private final SocketFactory _socketFactory;
	private final Delegate _delegate;
	private Socket _socket;
	private OutputStream _out;
	private InputStream _in;
	private boolean _watchingVariables;
	private volatile boolean _shouldBeRunning;
	final ArrayBlockingQueue<String> _outputQueue;

	public RWorker(SocketFactory socketFactory, Delegate delegate) {
		//create a default socket factory if one was not passed to us
		if (null == socketFactory) {
			try { 
				socketFactory = new SocketFactory(); 
			} catch (Exception e) { 
				log.error("failed to create default socket factory", e);
			}
		}
		_socketFactory = socketFactory;
		_delegate = delegate;
		_outputQueue = new ArrayBlockingQueue<String>(OUT_QUEUE_SIZE);
}
	
	public Delegate getDelegate() { return _delegate; }

	public boolean getWatchingVariables() { return _watchingVariables; }
	public void setWatchingVariables(boolean watch) {
		_watchingVariables = watch;
		// notify server
		Map<String, Object> cmd = new HashMap<String, Object>();
		cmd.put("msg", "toggleVariableWatch");
		cmd.put("argument", "");
		cmd.put("watch", watch);
		_outputQueue.add(serializeJson(cmd));
	}

	public String serializeJson(Map<String,Object> jo) {
		try {
			if (_delegate != null)
				return _delegate.getObjectMapper().writeValueAsString(jo);
		} catch (Exception e) {
			log.error("failed to serialize json", e);
		}
		return null;
	}

	public boolean isIdle() {
		if (_outputQueue.peek() != null)
			return false;
		return true;
	}

	public void shutdown() {
		_shouldBeRunning = false; //this will end i/o threads
		try {
			//clear queue and then write from this thread instead of output thread
			while (!_outputQueue.isEmpty())
				_outputQueue.remove();
			writeOutMessage("{\"msg\":\"close\", \"argument\":\"\"}");

			if (_in != null)
				_in.close();
			if (_out != null)
				_out.close();
			if (_socket != null && _socket.isConnected() && !_socket.isClosed())
				_socket.close();
		} catch (Exception e) {
			log.warn("exception closing socket", e);
		}
	}

	public void executeScript(String scriptCode) {
		Map<String, Object> cmd = new HashMap<String, Object>();
		cmd.put("msg", "execScript");
		cmd.put("argument", scriptCode);
		cmd.put("startTime", Long.toString(System.currentTimeMillis()));
		_outputQueue.add(serializeJson(cmd));
	}

	public void executeScriptFile(int fileId) {
		Map<String, Object> cmd = new HashMap<String, Object>();
		cmd.put("msg", "execFile");
		cmd.put("argument", Integer.toString(fileId));
		cmd.put("startTime", Long.toString(System.currentTimeMillis()));
		Map<String, Object> clientData = new HashMap<String, Object>();
		clientData.put("fileId", fileId);
		cmd.put("clientData", clientData);
		log.info("sending file cmd:" + cmd);
		_outputQueue.add(serializeJson(cmd));
	}

	public void lookupInHelp(String topic) {
		Map<String, Object> cmd = new HashMap<String, Object>();
		cmd.put("msg", "help");
		cmd.put("argument", topic);
		_outputQueue.add(serializeJson(cmd));
	}

	public void fetchVariableValue(String varName) {
		if (varName == null) {
			// list all variables
			listVariables(false);
		} else {
			// request one variable
			Map<String, Object> cmd = new HashMap<String, Object>();
			cmd.put("msg", "getVariable");
			cmd.put("argument", varName);
			_outputQueue.add(serializeJson(cmd));
		}
	}

	protected void listVariables(boolean deltaOnly) {
		Map<String, Object> cmd = new HashMap<String, Object>();
		cmd.put("msg", "listVariables");
		cmd.put("argument", "");
		cmd.put("delta", deltaOnly);
		_outputQueue.add(serializeJson(cmd));
	}
	
	private void writeOutMessage(String msgstr) throws IOException {
		if (null == _out)
			return;
		if (null == msgstr)
			return;
		byte[] msgbytes = msgstr.getBytes(Charset
				.forName("UTF-8"));

		ByteBuffer bb = ByteBuffer.allocate(8);
		IntBuffer ib = bb.asIntBuffer();
		ib.put(0, MAGIC_IDENT);
		ib.put(1, msgbytes.length);
		_out.write(bb.array());

		_out.write(msgbytes);
		_out.flush();
		
	}
	
	void handleJsonResponse(String jsonString) {
		try {
			log.debug("response:" + jsonString);
			BaseRResponse bm = _delegate.getObjectMapper().readValue(jsonString, BaseRResponse.class);
			final String methodName = "handle" + bm.getClass().getSimpleName().replace("RR", "R");
			Method m = getClass().getDeclaredMethod(methodName, bm.getClass());
			m.invoke(this, bm);
		} catch (Exception e) {
			log.error("error handling json response:" + jsonString, e);
		}
	}
	
	@SuppressWarnings("unused") //dynamically called
	private void handleErrorResponse(ErrorRResponse msg) {
		ErrorResponse  rsp = new ErrorResponse(msg.getDetails());
		getDelegate().broadcastToAllClients(rsp);
	}

	@SuppressWarnings("unused") //dynamically called
	private void handleExecCompleteResponse(ExecCompleteRResponse msg) {
		int batchId = msg.getImageBatchId();
		List<RCSessionImage> images=null;
		if (batchId > 0) {
			images = getDelegate().getDAO().findImageBatchById(batchId);
		}
		ExecCompleteResponse rsp = new ExecCompleteResponse(batchId , images);
		getDelegate().broadcastToAllClients(rsp);
	}

	@SuppressWarnings("unused") //dynamically called
	private void handleHelpResponse(HelpRResponse msg) {
		HelpResponse rsp = new HelpResponse(msg.getTopic(), msg.getPaths());
		getDelegate().broadcastToAllClients(rsp);
	}

	@SuppressWarnings("unused") //dynamically called
	private void handleResultsResponse(ResultsRResponse msg) {
		getDelegate().broadcastToAllClients(new ResultsResponse(msg.getString(), 0));
	}

	@SuppressWarnings("unused") //dynamically called
	private void handleShowOutputResponse(ShowOutputRResponse msg) {
		getDelegate().broadcastToAllClients(new ResultsResponse("", msg.getFileId()));
	}

	@SuppressWarnings("unused") //dynamically called
	private void handleVariableUpdateResponse(VariableUpdateRResponse msg) {
		VariableResponse rsp = new VariableResponse(msg.getVariables(), msg.getUserIdentifier(), msg.isDelta(), false);
		getDelegate().broadcastToAllClients(rsp);
	}

	@SuppressWarnings("unused") //dynamically called
	private void handleVariableValueResponse(VariableValueRResponse msg) {
		VariableResponse rsp = new VariableResponse(msg.getValue(), msg.getUserIdentifier(), false, 
				msg.getUserIdentifier() > 0);
		if (rsp.isSingleValue())
			getDelegate().broadcastToSingleClient(rsp, rsp.getSocketId());
		else
			getDelegate().broadcastToAllClients(rsp);
	}

	@Override
	public void run() {
		_shouldBeRunning = true;
		try {
			synchronized (this) {
				_socket = _socketFactory.createSocket();
				log.info("rworker connected");
				_in = _socket.getInputStream();
				_out = _socket.getOutputStream();
				RWorkerInputThread ith = new RWorkerInputThread();
				RWorkerOutputThread oth = new RWorkerOutputThread();
				_shouldBeRunning = true;
				ith.start();
				oth.start();
			}
			Map<String, Object> jo = new HashMap<String, Object>();
			jo.put("msg", "open");
			jo.put("argument", "");
			jo.put("wspaceId", _delegate.getWorkspaceId());
			jo.put("sessionRecId", _delegate.getSessionRecordId());
			jo.put("dbhost", _delegate.getDAO().getDBHost());
			jo.put("dbuser", _delegate.getDAO().getDBUser());
			jo.put("dbname", _delegate.getDAO().getDBDatabase());
			_outputQueue.add(serializeJson(jo));
			// we keep our thread looping
			synchronized (this) {
				this.notifyAll();
			}
		} catch (Exception e) {
			log.error("exception in client main loop", e);
			if (null != _delegate)
				_delegate.clientHadError(e);
		}
		log.debug("client main loop ended");
	}

	public interface Delegate {
		public int getWorkspaceId();
		public int getSessionRecordId();
		public ObjectMapper getObjectMapper();
		public Rc2DAO getDAO();

		public void broadcastToAllClients(BaseResponse response);
		public void broadcastToSingleClient(BaseResponse response, int socketId);

		public void clientHadError(Exception e);
		public void connectionFailed(Exception e);
	}

	private class RWorkerInputThread extends Thread {
		@Override
		public void run() {
			try {
				byte[] header = new byte[8];
				IntBuffer buf = ByteBuffer.wrap(header).asIntBuffer();
				byte[] genericBuffer = new byte[4096];
				while (_shouldBeRunning) {
					int readCount = _in.read(header, 0, 8);
					if (readCount != 8) {
						log.warn("read got EOF mid header");
						return;
					}
					if (buf.get(0) != MAGIC_IDENT) {
						log.warn("got invalid magic number:" + buf.get(0));
						continue;
					}
					int bufSize = buf.get(1);
					// if less than 4K, use a static buffer instead of
					// constantly alloc'ing new buffers
					byte[] jsonBuffer = bufSize < genericBuffer.length ? genericBuffer : new byte[bufSize];
					readCount = _in.read(jsonBuffer, 0, bufSize);
					if (readCount != bufSize) {
						log.warn("failed to read correct number of bytes");
						continue;
					}
					String json = new String(jsonBuffer, 0, bufSize,
							Charset.forName("UTF-8"));
					handleJsonResponse(json);
				}
			} catch (Exception e) {
				if (null != _delegate && e instanceof SocketException) {
					_delegate.connectionFailed(e);
				} else {
					if (!e.getMessage().contains("Socket closed"))
						log.warn("exception in input thread", e);
				}
			}
		}
	}

	private class RWorkerOutputThread extends Thread {
		@Override
		public void run() {
			try {
				while (_shouldBeRunning) {
					String msg = _outputQueue.poll(2,
							TimeUnit.SECONDS);
					if (msg != null) {
						log.debug("sending message:" + msg);
						writeOutMessage(msg);
					}
				}
			} catch (Exception e) {
				if (null != _delegate && e instanceof SocketException) {
					_delegate.connectionFailed(e);
				} else {
					if (!e.getMessage().contains("Socket closed"))
						log.warn("exception in output thread", e);
				}
			}
		}
	}
	
	/**
	 	allows abstraction of socket creation (i.e. mocking for unit tests
	 */
	public static class SocketFactory {
		private String _host;
		private int _port;
		public SocketFactory() {
			this("localhost", 7714);
		}
		public SocketFactory(String host) {
			this(host, 7714);
		}
		public SocketFactory(String host, int port) {
			super();
			_host = host;
			_port = port;
		}
		public Socket createSocket() throws UnknownHostException, IOException {
			return createSocket(_host, _port);
		}
		public Socket createSocket(String host, int port) throws UnknownHostException, IOException {
			return new Socket(host, port);
		}
	}
}
