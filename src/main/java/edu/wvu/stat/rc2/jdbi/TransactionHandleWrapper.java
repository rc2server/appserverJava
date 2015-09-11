package edu.wvu.stat.rc2.jdbi;

import java.sql.SQLException;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

/** Privately manages a handle that has autocommit disabled. 
*/

public class TransactionHandleWrapper implements AutoCloseable {
	Handle _h;
	
	public TransactionHandleWrapper(DBI dbi) {
		_h = dbi.open();
		try {
			_h.getConnection().setAutoCommit(false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		_h.begin();
	}

	public <T> T addDao(Class<T> type) {
		return _h.attach(type);
	}
	
	public void rollback() {
		_h.rollback();
	}
	
	public void commit() {
		if (_h.isInTransaction())
			_h.commit();
	}

	/** commits the transaction and closes the open Handle, but not the DBI. */
	@Override
	public void close() {
		if (_h.isInTransaction())
			_h.commit();
		_h.close();
	}
}
