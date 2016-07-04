package edu.wvu.stat.rc2.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

@AutoValue
public abstract class RCProject {

	@JsonCreator
	public static RCProject create(
			@JsonProperty("id") int id, 
			@JsonProperty("version") int version, 
			@JsonProperty("userId") int userId, 
			@JsonProperty("name") String name) 
	{
		return new AutoValue_RCProject(id, version, userId, name);
	}

	public abstract @JsonProperty int getId();
	public abstract @JsonProperty int getVersion();
	public abstract @JsonProperty int getUserId();
	public abstract @JsonProperty String getName();

	private List<RCWorkspace> _wspaces;
	public List<RCWorkspace> getWorkspaces() { return _wspaces; }
	public void setWorkspaces(List<RCWorkspace> wspaces) {
		_wspaces = ImmutableList.copyOf(wspaces);
	}

	
	static RCProject createFromResultSet(ResultSet rs) throws SQLException {
		return new AutoValue_RCProject(rs.getInt("id"), rs.getInt("version"), rs.getInt("userId"), rs.getString("name"));
	}

	public static class RCProjectMapper implements ResultSetMapper<RCProject> {
		public RCProjectMapper() {}
		public RCProject map(int index, ResultSet rs, StatementContext ctx) throws SQLException {
			return RCProject.createFromResultSet(rs);
		}
	}
}
