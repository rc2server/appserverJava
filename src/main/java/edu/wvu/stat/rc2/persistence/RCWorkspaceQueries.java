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

	@SqlQuery("select * from RCWorkspace where projectId = :projectId and name ilike :name")
	@Mapper(RCWorkspaceMapper.class)
	public abstract RCWorkspace findByNameAndProject(@Bind("name") String name, @Bind("projectId") int projectId);

	@SqlQuery("select * from RCWorkspace where projectid = :projectid order by name")
	@Mapper(RCWorkspaceMapper.class)
	public abstract List<RCWorkspace> forProject(@Bind("projectid") int projectid);

	@SqlQuery("select * from RCWorkspace where userid = :userid order by name")
	@Mapper(RCWorkspaceMapper.class)
	public abstract List<RCWorkspace> ownedByUser(@Bind("userid") int userid);
	
	@SqlQuery("insert into rcworkspace (name, userid, projectid) values (:name, :userid, :projectid) returning id")
	public abstract int createWorkspace(@Bind("name") String name, @Bind("projectid") int projectid, @Bind("userid") int userid);
	
	@SqlUpdate("update rcworkspace set name = :name where id = :id")
	public abstract int updateWorkspace(@Bind("id") int id, @Bind("name") String name);
	
	@SqlUpdate("delete from rcworkspace where id = :id")
	public abstract int deleteWorkspace(@Bind("id") int id);
	
	public RCWorkspace findByIdIncludingFiles(@Bind("id") int id) {
		RCFileQueries fileDao = getHandle().attach(RCFileQueries.class);
		RCWorkspace wspace = findById(id);
		wspace.setFiles(fileDao.filesForWorkspaceId(id));
		return wspace;
	}
	
	public List<RCWorkspace> ownedByUserIncludingFiles(@Bind("userid") int userid) {
		List<RCWorkspace> wspaces = ownedByUser(userid);
		RCFileQueries fileDao = getHandle().attach(RCFileQueries.class);
		for (RCWorkspace wspace : wspaces) {
			wspace.setFiles(fileDao.filesForWorkspaceId(wspace.getId()));
		}
		return wspaces;
	}

	public List<RCWorkspace> forProjectIncludingFiles(@Bind("projectid") int projectid) {
		List<RCWorkspace> wspaces = forProject(projectid);
		RCFileQueries fileDao = getHandle().attach(RCFileQueries.class);
		for (RCWorkspace wspace : wspaces) {
			wspace.setFiles(fileDao.filesForWorkspaceId(wspace.getId()));
		}
		return wspaces;
	}
}
