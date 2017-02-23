package edu.wvu.stat.rc2.persistence;

import java.util.List;

import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wvu.stat.rc2.jdbi.TransactionHandleWrapper;

public class Rc2DAO {
	static final Logger log = LoggerFactory.getLogger("rc2.Rc2DAO");

	private final DBI _dbi;
	private final String _dbHost;
	private final String _dbUser;
	private final String _dbDatabase;
	private volatile RCUser.Queries _userDao;
	private volatile RCProjectQueries _projDao;
	private volatile RCWorkspaceQueries _wsDao;
	private volatile RCSessionImage.Queries _imgDao;
	private volatile RCFileQueries _fileDao;
	
	Rc2DAO(DBI dbi, String host, String user, String database) {
		_dbi = dbi;
		_dbHost = host;
		_dbUser = user;
		_dbDatabase = database;
	}
	
	public String getDBHost() { return _dbHost; }
	public String getDBUser() { return _dbUser; }
	public String getDBDatabase() { return _dbDatabase; }
	
	public TransactionHandleWrapper createTransactionWrapper() {
		return new TransactionHandleWrapper(_dbi);
	}
	
	public RCUser findUserById(int userId) {
		return getUserDao().findById(userId);
	}
	
	public RCProject findProjectById(int projid) {
		return getProjectDao().findById(projid);
	}
	
	public RCWorkspace findWorkspaceById(int wsid) {
		RCWorkspace wspace = getWorkspaceDao().findById(wsid);
		wspace.setProject(findProjectById(wspace.getProjectId()));
		return wspace;
	}
	
	public RCSessionImage findImageById(int imageId) {
		return getSessionImageDao().findById(imageId);
	}

	public List<RCSessionImage> findImageBatchById(int batchId, int sessionId) {
		return getSessionImageDao().findByBatchId(batchId, sessionId);
	}
	
	//uses double check idiom for fast performance (25x over synchronized)
	public RCSessionImage.Queries getSessionImageDao() {
		RCSessionImage.Queries result = _imgDao;
		if (null == result) {
			synchronized(this) {
				result = _imgDao;
				if (result == null)
					_imgDao = result = _dbi.onDemand(RCSessionImage.Queries.class);
			}
		}
		return result;
	}
	
	//uses double check idiom for fast performance (25x over synchronized)
	public RCProjectQueries getProjectDao() {
		RCProjectQueries result = _projDao;
		if (null == result) {
			synchronized(this) {
				result = _projDao;
				if (result == null)
					_projDao = result = _dbi.onDemand(RCProjectQueries.class);
			}
		}
		return result;
	}

	//uses double check idiom for fast performance (25x over synchronized)
	public RCWorkspaceQueries getWorkspaceDao() {
		RCWorkspaceQueries result = _wsDao;
		if (null == result) {
			synchronized(this) {
				result = _wsDao;
				if (result == null)
					_wsDao = result = _dbi.onDemand(RCWorkspaceQueries.class);
			}
		}
		return result;
	}
	
	//uses double check idiom for fast performance (25x over synchronized)
	public RCFileQueries getFileDao() {
		RCFileQueries result = _fileDao;
		if (null == result) {
			synchronized(this) {
				result = _fileDao;
				if (null == result) {
					_fileDao = result = _dbi.onDemand(RCFileQueries.class);
					if (null == _fileDao) {
						log.error("failed to create fileDAO");
					}
				}
			}
		}
		return result;
	}
	
	//uses double check idiom for fast performance (25x over synchronized)
	public RCUser.Queries getUserDao() {
		RCUser.Queries result = _userDao;
		if (null == result) {
			synchronized(this) {
				result = _userDao;
				if (result == null)
					_userDao = result = _dbi.onDemand(RCUser.Queries.class);
			}
		}
		return result;
	}
	
	public DBI getDBI() { return _dbi; }
}
