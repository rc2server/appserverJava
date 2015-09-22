package edu.wvu.stat.rc2.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class RCSessionImage {

	@JsonCreator
	public static RCSessionImage create(
			@JsonProperty("id") int id, 
			@JsonProperty("sessionId") int sessionId, 
			@JsonProperty("batchId") int batchId, 
			@JsonProperty("nameId") String name, 
			@JsonProperty("dateCreated") Date dateCreated, 
			@JsonProperty("imageData") byte[] imgData) 
	{
		return new AutoValue_RCSessionImage(id, sessionId, batchId, name, dateCreated, imgData);
	}

	public abstract @JsonProperty int getId();
	public abstract @JsonProperty int getSessionId();
	public abstract @JsonProperty int getBatchId();
	public abstract @JsonProperty String getName();
	public abstract @JsonProperty Date getDateCreated();
	public abstract byte[] getImageData();
	
	static RCSessionImage createFromResultSet(ResultSet rs) throws SQLException {
		return new AutoValue_RCSessionImage(rs.getInt("id"), rs.getInt("sessionid"), rs.getInt("batchid"),
				rs.getString("name"), rs.getDate("datecreated"), rs.getBytes("imgdata"));
	}

	public static class RCSessionImageMapper implements ResultSetMapper<RCSessionImage> {
		public RCSessionImageMapper() {}
		public RCSessionImage map(int index, ResultSet rs, StatementContext ctx) throws SQLException {
			return RCSessionImage.createFromResultSet(rs);
		}
	}

	public interface Queries {
		@SqlQuery("select * from sessionimage where id = :id")
		@Mapper(RCSessionImageMapper.class)
		RCSessionImage findById(@Bind("id") int id);

		@SqlQuery("select * from sessionimage where batchid = :batchid")
		@Mapper(RCSessionImageMapper.class)
		List<RCSessionImage> findByBatchId(@Bind("batchid") int batchid);
}
}
