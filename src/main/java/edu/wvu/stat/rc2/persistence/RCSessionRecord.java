package edu.wvu.stat.rc2.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class RCSessionRecord {

	@JsonCreator
	public static RCSessionRecord create(
			@JsonProperty("id") int id, 
			@JsonProperty("workspaceId") int wspaceId, 
			@JsonProperty("startDate") Date startDate, 
			@JsonProperty("endDate") Date endDate) 
	{
		return new AutoValue_RCSessionRecord(id, wspaceId, startDate, endDate);
	}

	public abstract @JsonProperty int getId();
	public abstract @JsonProperty int getWorkspaceId();
	public abstract @JsonProperty Date getStartDate();
	public abstract @JsonProperty Date getEndDate();

	static RCSessionRecord createFromResultSet(ResultSet rs) throws SQLException {
		return new AutoValue_RCSessionRecord(rs.getInt("id"), rs.getInt("wspaceid"), rs.getDate("startDate"), rs.getDate("endDate"));
	}

	public static class RCSessionRecordMapper implements ResultSetMapper<RCSessionRecord> {
		public RCSessionRecordMapper() {}
		public RCSessionRecord map(int index, ResultSet rs, StatementContext ctx) throws SQLException {
			return RCSessionRecord.createFromResultSet(rs);
		}
	}

	public interface Queries {
		@SqlQuery("select * from sessionrecord where id = :id")
		@Mapper(RCSessionRecordMapper.class)
		RCSessionRecord findById(@Bind("id") int id);
		
		@SqlUpdate("insert into sessionrecord (wspaceId) values (:wspaceId)")
		@GetGeneratedKeys
		public int createSessionRecord(@Bind("wspaceId") int wspaceId);

		@SqlUpdate("update sessionrecord set enddate = now() where id = :id")
		public int closeSessionRecord(@Bind("id") int id);
	}
}
