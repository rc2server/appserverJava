package edu.wvu.stat.rc2.persistence;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RCLoginToken {
	private final int _id;
	private final Date _dateCreated;
	private final int _userId;
	private final BigInteger _seriesIdent;
	private final BigInteger _tokenIdent;
	
	@JsonCreator
	RCLoginToken(int id, int userid, Date dateCreated, BigInteger series, BigInteger token) {
		_id = id;
		_userId = userid;
		_dateCreated = dateCreated;
		_seriesIdent = series;
		_tokenIdent = token;
	}

	@JsonProperty("id")
	public int getId() {
		return _id;
	}

	@JsonProperty("dateCreated")
	public Date getDateCreated() {
		return _dateCreated;
	}

	@JsonProperty("userId")
	public int getUserId() {
		return _userId;
	}

	@JsonProperty("seriesIdent")
	public BigInteger getSeriesIdent() {
		return _seriesIdent;
	}

	@JsonProperty("tokenIdent")
	public BigInteger getTokenIdent() {
		return _tokenIdent;
	}

	@JsonIgnore
	public String getCookieValue() {
		return getUserId() + "_" + getTokenIdent() + "_" + getSeriesIdent();
	}

	
	public static class LoginTokenMapper implements ResultSetMapper<RCLoginToken> {
		public LoginTokenMapper() {}
		public RCLoginToken map(int index, ResultSet rs, StatementContext ctx) throws SQLException {
			return new RCLoginToken(rs.getInt("id"), rs.getInt("userid"), rs.getTimestamp("datecreated"),
					rs.getBigDecimal("series_ident").toBigInteger(),
					rs.getBigDecimal("token_ident").toBigInteger());
		}
	}
	
	//Queries are in separate file because of limitation of jdbi when using abstract class instead of interface
}
