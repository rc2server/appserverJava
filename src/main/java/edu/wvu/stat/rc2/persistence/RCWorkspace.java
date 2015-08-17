package edu.wvu.stat.rc2.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

@AutoValue
@JsonIgnoreProperties(value={"files"}, allowGetters=true)
public abstract class RCWorkspace {
	
	@JsonCreator
	public static RCWorkspace create(
			@JsonProperty("id") int id, 
			@JsonProperty("version") int version, 
			@JsonProperty("userId") int userId, 
			@JsonProperty("name") String name) 
	{
		return new AutoValue_RCWorkspace(id, version, userId, name);
	}
	
	public abstract @JsonProperty int getId();
	public abstract @JsonProperty int getVersion();
	public abstract @JsonProperty int getUserId();
	public abstract @JsonProperty String getName();

	static RCWorkspace createFromResultSet(ResultSet rs) throws SQLException {
		return new AutoValue_RCWorkspace(rs.getInt("id"), rs.getInt("version"), rs.getInt("userId"), rs.getString("name"));
	}
	
	private List<RCFile> _files;
	public List<RCFile> getFiles() { return _files; }
	public void setFiles(List<RCFile> files) {
		_files = ImmutableList.copyOf(files);
	}
	
	
	
	public static class RCWorkspaceMapper implements ResultSetMapper<RCWorkspace> {
		public RCWorkspaceMapper() {}
		public RCWorkspace map(int index, ResultSet rs, StatementContext ctx) throws SQLException {
			return RCWorkspace.createFromResultSet(rs);
		}
	}
	
	//queries in RCWorkspaceQueries
}
