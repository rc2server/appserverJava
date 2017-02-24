package edu.wvu.stat.rc2.ws;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import edu.wvu.stat.rc2.persistence.RCFile;
import edu.wvu.stat.rc2.persistence.RCWorkspace;
import edu.wvu.stat.rc2.persistence.Rc2DAO;
import edu.wvu.stat.rc2.rworker.RWorker;
import edu.wvu.stat.rc2.ws.response.BaseResponse;
import edu.wvu.stat.rc2.ws.response.FileChangedResponse;
import edu.wvu.stat.rc2.ws.response.FileChangedResponse.ChangeType;

public class FileUpdateCenter {
	static final Logger log = LoggerFactory.getLogger("rc2.FileUpdateCenter");
	static final int insertFakeId = -2;
	
	private final ExecutorService _executor;
	private SetMultimap<Integer, Callback> _callbacks;
	private SetMultimap<String, Callback> _insertCallbacks;
	private final Delegate _delegate;

	public FileUpdateCenter(Delegate delegate, ExecutorService executor) {
		_delegate = delegate;
		_callbacks = HashMultimap.create();
		_insertCallbacks = HashMultimap.create();
		_executor = executor;
	}
	
	public void addUpdateCallback(int fileId, int previousVersion, Consumer<RCFile> action) {
		_callbacks.put(fileId, new Callback(CallbackType.UPDATE, fileId, previousVersion, null, action));
	}

	public void addInsertCallback(String fileName, Consumer<RCFile> action) {
		_insertCallbacks.put(fileName, new Callback(CallbackType.INSERT, insertFakeId, 0, fileName, action));
	}

	public void addDeleteCallback(int fileId, int previousVersion, Consumer<RCFile> action) {
		log.info("adding delete callback for " + fileId);
		_callbacks.put(fileId, new Callback(CallbackType.DELETE, fileId, previousVersion, null, action));
	}

	public void databaseFileUpdated(String message, RWorker rworker) {
		_executor.execute(() -> processFileUpdate(message, rworker));
	}
	
	private void performCallbacks(CallbackType type, int fileId, RCFile file) {
		log.info("performing callbacks");
		if (type == CallbackType.INSERT) {
			for (Callback callback : _insertCallbacks.get(file.getName())) {
				_executor.execute(() -> callback.action.accept(file));
				_insertCallbacks.remove(file.getName(), callback);
			}
			return;
		}
		for (Callback callback : _callbacks.get(fileId)) {
			if (callback.type == type)  {
				if (callback.matches(type, fileId, file)) {
					_executor.execute(() -> callback.action.accept(file));
				}
				_callbacks.remove(fileId, callback);
			}
		}
	}
	
	private void processFileUpdate(String message, RWorker rworker) {
		log.info("got db file notification:" + message);
		String[] parts = message.split("/");
		int fid = Integer.parseInt(parts[0].substring(1));
		int wspaceid = Integer.parseInt(parts[1]);
		if (wspaceid != _delegate.getWorkspace().getId())
			return;
		log.info("db notification is for us, file " + fid);
		ChangeType changeType = ChangeType.fromString(message);
		RCWorkspace wspace = _delegate.getWorkspace();
		//force refresh from the database. ideally we should be more selective, but the performance likely doesn't matter
		wspace.setFiles(_delegate.getDAO().getFileDao().filesForWorkspaceId(wspace.getId()));
		Optional<RCFile> file = wspace.getFileWithId(fid);
		if (changeType != ChangeType.Delete) {
			if (!file.isPresent()) {
				//try fetching that particular file
				RCFile fetchedFile = _delegate.getDAO().getFileDao().findById(fid);
				if (fetchedFile == null) {
					log.warn(String.format("got file notification '%s' for unknown file %d in workspace %d", message, fid, wspaceid));
					return;
				}
				log.info("manually inserted file in workspace");
				List<RCFile> files = wspace.getFiles();
				files.add(fetchedFile);
				wspace.setFiles(files);
				file = Optional.of(fetchedFile);
			} else {
				log.info("file isn't present");
			}
		}
		log.info("preparing client change notification");
		final RCFile actualFile = file.isPresent() ? file.get() : null;
		FileChangedResponse  rsp = new FileChangedResponse(fid, actualFile, changeType);
		log.info("sending change notification");
		_delegate.broadcastToAllClients(rsp);
		_executor.submit(() -> {
			rworker.fileUpdated(actualFile);
		});
		performCallbacks(CallbackType.forString(parts[0].substring(0, 1)), fid, actualFile);
	}
	
	enum CallbackType { 
		INSERT, UPDATE, DELETE;
		
		static CallbackType forString(String s) {
			if (s.equals("i")) return INSERT;
			if (s.equals("d")) return DELETE;
			if (s.equals("u")) return UPDATE;
			throw new RuntimeException("invalid callback type string");
		}
	}
	
	class Callback {
		final int fileId;
		final CallbackType type;
		final int lastVersion;
		final String insertedName;
		final Consumer<RCFile> action;
		
		Callback(CallbackType type, int fileId, int lastVersion, String insertedName, Consumer<RCFile> action) {
			this.type = type;
			this.fileId = fileId;
			this.insertedName = insertedName;
			this.action = action;
			this.lastVersion = lastVersion;
		}
		
		boolean matches(CallbackType type, int fileId, RCFile file) {
			if (this.type != type) return false;
			if (type == CallbackType.INSERT && file.getName().equals(insertedName))
				return true;
			int version = file == null ? -1 : file.getVersion();
			log.info(String.format("comparing version %d to %d", version, lastVersion));
			//only call if file has changed
			return ((file == null && type == CallbackType.DELETE) || 
				(file.getVersion() > lastVersion));
		}
	}
	
	public interface Delegate {
		public Rc2DAO getDAO();
		public RCWorkspace getWorkspace();
		public void broadcastToAllClients(BaseResponse response);
		public void broadcastToSingleClient(BaseResponse response, int socketId);
	}
}
