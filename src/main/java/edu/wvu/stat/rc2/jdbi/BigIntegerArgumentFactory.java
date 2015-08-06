package edu.wvu.stat.rc2.jdbi;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

public class BigIntegerArgumentFactory implements ArgumentFactory<BigInteger> {

	@Override
	public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
		return value instanceof BigInteger;
	}

	@Override
	public Argument build(Class<?> expectedType, BigInteger value, StatementContext ctx) {
		return new Argument() {
			@Override
			public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
				statement.setBigDecimal(position, new BigDecimal(value));
			}
		};
	}
}
