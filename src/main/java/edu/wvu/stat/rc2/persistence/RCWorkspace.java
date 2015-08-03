package edu.wvu.stat.rc2.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class RCWorkspace implements PersistentObject {
	private final int _id, _version, _userId;
	private final String _name;
	
	@JsonCreator
	public RCWorkspace(@JsonProperty("id") int id, @JsonProperty("version") int version, 
			@JsonProperty("userId") int userId, @JsonProperty("name") String name) 
	{
		_id = id;
		_version = version;
		_userId = userId;
		_name = name;
	}
	
	@JsonProperty("id")
	public int getId() { return _id; }
	
	@JsonProperty("version")
	public int getVersion() { return _version; }
	
	@JsonProperty("userId")
	public int getUserId() { return _userId; }
	
	@JsonProperty("name")
	public String getName() { return _name; }
	
	public static class RCWorkspaceMapper implements ResultSetMapper<RCWorkspace> {
		public RCWorkspaceMapper() {}
		public RCWorkspace map(int index, ResultSet rs, StatementContext ctx) throws SQLException {
			RCWorkspace obj = new RCWorkspace(rs.getInt("id"), rs.getInt("version"), rs.getInt("userid"), rs.getString("name"));
			return obj;
		}
	}
	
	public interface Queries {
		@SqlQuery("select * from RCWorkspace where id = :id")
		@Mapper(RCWorkspaceMapper.class)
		RCWorkspace findById(@Bind("id") int id);

		@SqlQuery("select * from RCWorkspace where name ilike :name")
		@Mapper(RCWorkspaceMapper.class)
		RCWorkspace findByName(@Bind("name") String name);

		@SqlQuery("select * from RCWorkspace where userid = :userid")
		@Mapper(RCWorkspaceMapper.class)
		List<RCWorkspace> ownedByUser(@Bind("userid") int userid);
		
		@SqlQuery("insert into rcworkspace (name, userid) values (:name, :userid) returning id")
		int createWorkspace(@Bind("name") String name, @Bind("userid") int userid);
		
		@SqlUpdate("update rcworkspace set name = :name where id = :id")
		int updateWorkspace(@Bind("id") int id, @Bind("name") String name);
		
		@SqlUpdate("delete from rcworkspace where id = :id")
		int deleteWorkspace(@Bind("id") int id);
	}
}
