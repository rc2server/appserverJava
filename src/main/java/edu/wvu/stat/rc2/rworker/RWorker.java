package edu.wvu.stat.rc2.rworker;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wvu.stat.rc2.config.SessionConfig;
import edu.wvu.stat.rc2.persistence.RCFile;
import edu.wvu.stat.rc2.persistence.RCSessionImage;
import edu.wvu.stat.rc2.persistence.Rc2DAO;
import edu.wvu.stat.rc2.rworker.response.*;
import edu.wvu.stat.rc2.ws.response.*;

public class RWorker implements Runnable {
	static final Logger log = LoggerFactory.getLogger("rc2.RWorker");
	private static final int MAGIC_IDENT = 0x21;
	private static final int OUT_QUEUE_SIZE = 20;

	private final SocketFactory _socketFactory;
	private Delegate _delegate;
	private Socket _socket;
	private OutputStream _out;
	private DataInputStream _in;
	private int _nextQueryId;
	private boolean _watchingVariables;
	private volatile boolean _shouldBeRunning;
	final ArrayBlockingQueue<String> _outputQueue;
	private HashMap<Integer, ShowOutputRResponse> _pendingShowOutputs;

	public RWorker(SocketFactory socketFactory, Delegate delegate) {
		//new Exception().printStackTrace();
		//create a default socket factory if one was not passed to us
		if (null == socketFactory) {
			try { 
				socketFactory = new SocketFactory(delegate.getSessionConfig().getRComputeHost()); 
			} catch (Exception e) { 
				log.error("failed to create default socket factory", e);
			}
		}
		_socketFactory = socketFactory;
		_delegate = delegate;
		_outputQueue = new ArrayBlockingQueue<String>(OUT_QUEUE_SIZE);
		_nextQueryId = 1001;
		_pendingShowOutputs = new HashMap<Integer, ShowOutputRResponse>();
}
	
	public Delegate getDelegate() { return _delegate; }
	public void setDelegate(Delegate d) {
		if (null != _socket)
			throw new RuntimeException("can't set delegate once start has been called");
		_delegate = d;
	}

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
			//for some reason, this exception can happen when running unit tests and I can't figure out why
			try { if (_in != null) _in.close(); } catch (NullPointerException ne) {}
			try { if (_out != null) _out.close(); } catch (NullPointerException ne) {}
			if (_socket != null && _socket.isConnected() && !_socket.isClosed())
				_socket.close();
		} catch (Exception e) {
			log.warn("exception closing socket", e);
		}
	}

	public void executeScript(String scriptCode) {
		int queryId = _nextQueryId++;
		Map<String, Object> cmd = new HashMap<String, Object>();
		cmd.put("msg", "execScript");
		cmd.put("argument", scriptCode);
		cmd.put("queryId", queryId);
		cmd.put("startTime", Long.toString(System.currentTimeMillis()));
		_outputQueue.add(serializeJson(cmd));
		getDelegate().broadcastToAllClients(new EchoQueryResponse(queryId, 0, scriptCode));
	}

	public void executeScriptFile(int fileId) {
		int queryId = _nextQueryId++;
		Map<String, Object> cmd = new HashMap<String, Object>();
		cmd.put("msg", "execFile");
		cmd.put("argument", Integer.toString(fileId));
		cmd.put("startTime", Long.toString(System.currentTimeMillis()));
		cmd.put("queryId", queryId);
		Map<String, Object> clientData = new HashMap<String, Object>();
		clientData.put("fileId", fileId);
		cmd.put("clientData", clientData);
		log.info("sending file cmd:" + cmd);
		_outputQueue.add(serializeJson(cmd));
		getDelegate().broadcastToAllClients(new EchoQueryResponse(queryId, fileId, ""));
	}

	public void lookupInHelp(String topic) {
		Map<String, Object> cmd = new HashMap<String, Object>();
		cmd.put("msg", "help");
		cmd.put("argument", topic);
		_outputQueue.add(serializeJson(cmd));
	}

	public void saveEnvironment() {
		Map<String, Object> cmd = new HashMap<String, Object>();
		cmd.put("msg", "saveEnv");
		cmd.put("argument", "");
		_outputQueue.add(serializeJson(cmd));
		log.info("saving environment");
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

	public void fileUpdated(RCFile file) {
		ShowOutputRResponse rsp = _pendingShowOutputs.get(file.getId());
		if (null == rsp)
			log.info("fileUpdated called with unknown file id");
		if (rsp != null && file.getVersion() >= rsp.getFileVersion()) {
			sendShowOutputResponse(rsp, file);
			_pendingShowOutputs.remove(file.getId());
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
			log.info("getting images from batch " + batchId + ", sessionId " + _delegate.getSessionRecordId());
			images = getDelegate().getDAO().findImageBatchById(batchId, _delegate.getSessionRecordId());
		}
		ExecCompleteResponse rsp = new ExecCompleteResponse(batchId , images, msg.getQueryId(), msg.getExpectShowOutput());
		getDelegate().broadcastToAllClients(rsp);
	}

	@SuppressWarnings("unused") //dynamically called
	private void handleHelpResponse(HelpRResponse msg) {
		ArrayList<Map<String,String>> outItems = new ArrayList<Map<String,String>>();
		for (String aPath : msg.getPaths()) {
			Map<String,String> helpItem = new HashMap<String,String>();
			int loc = aPath.indexOf("/library/");
			String modPath = aPath.substring(loc+1);
			modPath = modPath.replace("/help/", "/html/");
			modPath = "http://www.stat.wvu.edu/rc2/" + modPath + ".html";
			helpItem.put("path", modPath);
			String[] components = aPath.split("/");
			String funName = components[components.length-1];
			funName = funName.substring(0, funName.lastIndexOf('.'));
			String pkgName = components.length > 3 ? components[components.length-3] : "Base";
			helpItem.put("title", funName + " (" + pkgName + ")");
			outItems.add(helpItem);
		}
		HelpResponse rsp = new HelpResponse(msg.getTopic(), outItems);
		getDelegate().broadcastToAllClients(rsp);
	}

	@SuppressWarnings("unused") //dynamically called
	private void handleResultsResponse(ResultsRResponse msg) {
		getDelegate().broadcastToAllClients(new ResultsResponse(msg.getString(), null, msg.getQueryId()));
	}

	@SuppressWarnings("unused") //dynamically called
	private synchronized void handleShowOutputResponse(ShowOutputRResponse msg) {
		RCFile file = getDelegate().getDAO().getFileDao().findById(msg.getFileId());
		if (file == null || file.getVersion() < msg.getFileVersion()) {
			//we need to delay sending this message until the file update notification is received from the database
			_pendingShowOutputs.put(msg.getFileId(), msg);
			log.info("delaying showoutput message want:" + msg.getFileVersion() + " actual:" + file.getVersion());
			return;
		}
		sendShowOutputResponse(msg, file);
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

	@SuppressWarnings("unused") //dynamically called
	private void handleOpenSuccessResponse(OpenSuccessRResponse rsp) {
		//TODO: report failed to open session to client(s)
	}
	
	private void sendShowOutputResponse(ShowOutputRResponse rsp, RCFile file) {
		byte[] fileData = null;
		if (file.getFileSize() <= getDelegate().getSessionConfig().getShowOutputFileSizeLimitInBytes()) {
			fileData = getDelegate().getDAO().getFileDao().fileDataById(file.getId());
		}
		getDelegate().broadcastToAllClients(new ShowOutputResponse(rsp.getQueryId(), file, fileData));
	}
	
	@Override
	public void run() {
		if (_delegate == null)
			throw new RuntimeException("can't start when delegate not set");
		_shouldBeRunning = true;
		try {
			synchronized (this) {
				_socket = _socketFactory.createSocket();
				log.info("rworker connected");
				_in = new DataInputStream(_socket.getInputStream());
				_out = _socket.getOutputStream();
				RWorkerInputThread ith = new RWorkerInputThread();
				RWorkerOutputThread oth = new RWorkerOutputThread();
				ith.setName("rworker " + _delegate.getWorkspaceId() + " input thread");
				oth.setName("rworker " + _delegate.getWorkspaceId() + " output thread");
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
		public SessionConfig getSessionConfig();

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
					_in.readFully(header, 0, 8);
					if (buf.get(0) != MAGIC_IDENT) {
						log.warn("got invalid magic number:" + buf.get(0));
						continue;
					}
					int bufSize = buf.get(1);
					// if less than 4K, use a static buffer instead of
					// constantly alloc'ing new buffers
					byte[] jsonBuffer = bufSize < genericBuffer.length ? genericBuffer : new byte[bufSize];
					_in.readFully(jsonBuffer, 0, bufSize);
					String json = new String(jsonBuffer, 0, bufSize,
							Charset.forName("UTF-8"));
					handleJsonResponse(json);
				}
			} catch (Exception e) {
				if (null != _delegate && e instanceof SocketException) {
					_delegate.connectionFailed(e);
				} else {
					if (!(e instanceof EOFException) && !e.getMessage().contains("Socket closed"))
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
						log.info("sending message:" + msg);
						writeOutMessage(msg);
					}
				}
				log.info("rworker.output thread ending");
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
	 	allows abstraction of socket creation (i.e. mocking for unit tests)
	 */
	public static class SocketFactory {
		protected String _host;
		protected int _port;
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
			log.info("connecting to compute:" + host + ":" + port);
			return new Socket(host, port);
		}
	}
}
