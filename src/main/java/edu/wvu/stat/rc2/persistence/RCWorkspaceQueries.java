package edu.wvu.stat.rc2.persistence;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;

import edu.wvu.stat.rc2.persistence.RCWorkspace.RCWorkspaceMapper;

public abstract class RCWorkspaceQueries implements GetHandle {

	public RCWorkspaceQueries() {}
	
	@SqlQuery("select * from RCWorkspace where id = :id")
	@Mapper(RCWorkspaceMapper.class)
	public abstract RCWorkspace findById(@Bind("id") int id);

	@SqlQuery("select * from RCWorkspace where name ilike :name")
	@Mapper(RCWorkspaceMapper.class)
	public abstract RCWorkspace findByName(@Bind("name") String name);

	@SqlQuery("select * from RCWorkspace where userid = :userid order by name")
	@Mapper(RCWorkspaceMapper.class)
	public abstract List<RCWorkspace> ownedByUser(@Bind("userid") int userid);
	
	@SqlQuery("insert into rcworkspace (name, userid) values (:name, :userid) returning id")
	public abstract int createWorkspace(@Bind("name") String name, @Bind("userid") int userid);
	
	@SqlUpdate("update rcworkspace set name = :name where id = :id")
	public abstract int updateWorkspace(@Bind("id") int id, @Bind("name") String name);
	
	@SqlUpdate("delete from rcworkspace where id = :id")
	public abstract int deleteWorkspace(@Bind("id") int id);
	
	public List<RCWorkspace> ownedByUserIncludingFiles(@Bind("userid") int userid) {
		List<RCWorkspace> wspaces = ownedByUser(userid);
		RCFileQueries fileDao = getHandle().attach(RCFileQueries.class);
		for (RCWorkspace wspace : wspaces) {
			wspace.setFiles(fileDao.filesForWorkspaceId(wspace.getId()));
		}
		return wspaces;
	}
}
