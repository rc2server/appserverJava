package edu.wvu.stat.rc2.jdbi;

import java.sql.SQLException;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

/** Privately manages a handle that has autocommit disabled. 
*/

public class TransactionHandleWrapper implements AutoCloseable {
	private Handle _h;
	
	public TransactionHandleWrapper(DBI dbi) {
		_h = dbi.open();
		try {
			_h.getConnection().setAutoCommit(false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public <T> T addDao(Class<T> type) {
		return _h.attach(type);
	}
	
	/** closes the open Handle, but not the DBI. */
	@Override
	public void close() {
		_h.close();
	}
}
