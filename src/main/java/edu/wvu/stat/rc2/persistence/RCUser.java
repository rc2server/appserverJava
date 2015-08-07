package edu.wvu.stat.rc2.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RCUser implements PersistentObject {
	private int id, version;
	private String login, firstName, lastName, email, hashedPassword;
	private boolean admin, enabled;
	
	public int getId() { return this.id; }
	public int getVersion() { return this.version; }
	
	public String getLogin() { return this.login; }
	public String getFirstName() { return this.firstName; }
	public String getLastName() { return this.lastName; }
	public String getEmail() { return this.email; }
	public boolean isAdmin() { return this.admin; }
	public boolean isEnabled() { return this.enabled; }
	
	@JsonIgnore
	public String getHashedPassword() { return this.hashedPassword; }

	
	public static class RCUserMapper implements ResultSetMapper<RCUser> {
		public RCUserMapper() {}
		public RCUser map(int index, ResultSet rs, StatementContext ctx) throws SQLException {
			RCUser user = new RCUser();
			user.id = rs.getInt("id");
			user.version = rs.getInt("version");
			user.login = rs.getString("login");
			user.firstName = rs.getString("firstName");
			user.lastName = rs.getString("lastName");
			user.email = rs.getString("email");
			user.admin = rs.getBoolean("admin");
			user.enabled = rs.getBoolean("enabled");
			user.hashedPassword = rs.getString("passworddata");
			return user;
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
