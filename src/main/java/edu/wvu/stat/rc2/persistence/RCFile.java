package edu.wvu.stat.rc2.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import edu.wvu.stat.rc2.resources.LoginResource;

@AutoValue
public abstract class RCFile {
	final static Logger log = LoggerFactory.getLogger(LoginResource.class);

	@JsonCreator
	public static RCFile create(@JsonProperty("id") int id,
			@JsonProperty("wpaceId") int wspaceId,
			@JsonProperty("name") String name, 
			@JsonProperty("version") int version, 
			@JsonProperty("dateCreated") Date dateCreated, 
			@JsonProperty("lastModified") Date lastModified, 
			@JsonProperty("fileSize") int fileSize) 
	{
		return new AutoValue_RCFile(id, wspaceId, name, version, dateCreated, lastModified, fileSize);
	}

	static RCFile createFromResultSet(ResultSet rs) throws SQLException {
		return new AutoValue_RCFile(rs.getInt("id"), rs.getInt("wspaceid"), rs.getString("name"), 
				rs.getInt("version"), rs.getTimestamp("dateCreated"), rs.getTimestamp("lastModified"), 
				rs.getInt("fileSize"));
	}
	
	public abstract int getId();
	public abstract int getWspaceId();
	public abstract String getName();
	public abstract int getVersion();
	public abstract Date getDateCreated();
	public abstract Date getLastModified();
	public abstract int getFileSize();

	
	public static class RCFileMapper implements ResultSetMapper<RCFile> {
		public RCFileMapper() {}
		public RCFile map(int index, ResultSet rs, StatementContext ctx) throws SQLException {
			return RCFile.createFromResultSet(rs);
		}
	}
	
	/* queries are in the RCFileQueries class */ 
}
