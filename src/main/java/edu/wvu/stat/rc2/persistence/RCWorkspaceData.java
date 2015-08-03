package edu.wvu.stat.rc2.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.wvu.stat.rc2.resources.BaseResource;

public final class RCWorkspaceData extends BaseResource {
	private final int _id;
	private final byte[] _data;
	
	@JsonCreator
	public RCWorkspaceData(@JsonProperty("id") int id, @JsonProperty("data") byte[] data) {
		_id = id;
		_data = data;
	}
	
	public int getId() { return _id; }
	public byte[] getData() { return _data; }


	public static class RCWorkspaceDataMapper implements ResultSetMapper<RCWorkspaceData> {
		public RCWorkspaceDataMapper() {}
		public RCWorkspaceData map(int index, ResultSet rs, StatementContext ctx) throws SQLException {
			return new RCWorkspaceData(rs.getInt(1), rs.getBytes(2));
		}
	}

	public interface Queries {
		@SqlQuery("select id,bindata from rcworkspacedata where id = :id")
		@Mapper(RCWorkspaceDataMapper.class)
		RCWorkspaceData findById(@Bind("id") int id);
		
		@SqlUpdate("insert into rcworkspacedata (id, bindata) values (:id, :data)")
		int createData(@Bind("id") int id, @Bind("data") byte[] data);

		@SqlUpdate("update rcworkspacedata set bindata = :data where id = :id")
		int updateData(@Bind("data") byte[] data, @Bind("id") int id);
	}
}
