package edu.wvu.stat.rc2.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Nullable;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class RCUser {
	
	@JsonCreator
	public static RCUser create(@JsonProperty("id") int id,
			@JsonProperty("version") int version,
			@JsonProperty("login") String login, 
			@JsonProperty("firstName") @Nullable String firstName, 
			@JsonProperty("lastName") @Nullable String lastName, 
			@JsonProperty("email") String email, 
			@JsonProperty("admin") boolean admin, 
			@JsonProperty("enabled") boolean enabled, 
			@JsonProperty("hashedPassword") @Nullable String hashedPassword)
	{
		return new AutoValue_RCUser(id, version, login, firstName, lastName, email, admin, enabled, hashedPassword);
	}
	
	static RCUser createFromResultSet(ResultSet rs) throws SQLException {
		return new AutoValue_RCUser(rs.getInt("id"), rs.getInt("version"), rs.getString("login"),
				rs.getString("firstName"), rs.getString("lastName"), rs.getString("email"),
				rs.getBoolean("admin"), rs.getBoolean("enabled"), rs.getString("passworddata"));
	}
	
	public abstract int getId();
	public abstract int getVersion();
	
	public abstract String getLogin();
	@Nullable public abstract String getFirstName();
	@Nullable public abstract String getLastName();
	public abstract String getEmail();
	public abstract boolean isAdmin();
	public abstract boolean isEnabled();
	@JsonIgnore @Nullable public abstract String getHashedPassword();

	
	public static class RCUserMapper implements ResultSetMapper<RCUser> {
		public RCUserMapper() {}
		public RCUser map(int index, ResultSet rs, StatementContext ctx) throws SQLException {
			return RCUser.createFromResultSet(rs);
		}
	}
	
	public interface Queries {
		@SqlQuery("select * from rcuser where id = :id")
		@Mapper(RCUserMapper.class)
		RCUser findById(@Bind("id") int id);
		
		@SqlQuery("select * from rcuser where login = :login")
		@Mapper(RCUserMapper.class)
		RCUser findByLogin(@Bind("login") String login);
	}
}
