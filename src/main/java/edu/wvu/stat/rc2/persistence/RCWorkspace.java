package edu.wvu.stat.rc2.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class RCWorkspace implements PersistentObject {
	private int id, version, userId;
	private String name;
	
	public int getId() { return this.id; }
	public int getVersion() { return this.version; }
	
	public int getUserId() { return this.userId; }
	
	public String getName() { return this.name; }
	
	public static class RCWorkspaceMapper implements ResultSetMapper<RCWorkspace> {
		public RCWorkspaceMapper() {}
		public RCWorkspace map(int index, ResultSet rs, StatementContext ctx) throws SQLException {
			RCWorkspace obj = new RCWorkspace();
			obj.id = rs.getInt("id");
			obj.version = rs.getInt("version");
			obj.userId = rs.getInt("userid");
			obj.name = rs.getString("name");
			return obj;
		}
	}
	
	public interface Queries {
		@SqlQuery("select * from RCWorkspace where id = :id")
		@Mapper(RCWorkspaceMapper.class)
		RCWorkspace findById(@Bind("id") int id);
		
		@SqlQuery("select * from RCWorkspace where userid = :userid")
		@Mapper(RCWorkspaceMapper.class)
		List<RCWorkspace> ownedByUser(@Bind("userid") int userid);
	}
}
