package edu.wvu.stat.rc2.persistence;

import java.math.BigInteger;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import edu.wvu.stat.rc2.persistence.RCLoginToken.LoginTokenMapper;

public abstract class RCLoginTokenQueries {
	public RCLoginTokenQueries() {}
	
	@SqlQuery("select * from logintokens where userid = :userid and series_ident = :series")
	@Mapper(LoginTokenMapper.class)
	public abstract RCLoginToken findToken(@Bind("userid") int userid, @Bind("series") BigInteger series);

	@SqlQuery("select * from logintokens where id = :id")
	@Mapper(LoginTokenMapper.class)
	abstract RCLoginToken findTokenById(@Bind("id") int id);

	@SqlQuery("insert into logintokens (userid, series_ident, token_ident) values (:userid, :series, :token) returning id")
	protected abstract int createRawToken(@Bind("userid") int userid, @Bind("series") 
		long series, @Bind("token") long token);
	
	@Transaction
	public RCLoginToken createToken(int userid, long series, long token) {
		int tid = createRawToken(userid, series, token);
		return findTokenById(tid);
	}
}

