package edu.wvu.stat.rc2.persistence;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;

import edu.wvu.stat.rc2.persistence.RCProject.RCProjectMapper;

public abstract class RCProjectQueries implements GetHandle {
	public RCProjectQueries() {}
	
	@SqlQuery("select * from rcproject where id = :id")
	@Mapper(RCProjectMapper.class)
	public abstract RCProject findById(@Bind("id") int id);
	
	@SqlQuery("select * from RCProject where userid = :userid order by name")
	@Mapper(RCProjectMapper.class)
	public abstract List<RCProject> ownedByUser(@Bind("userid") int userid);

	public List<RCProject> ownedByUserIncludingWorkspacesAndFiles(@Bind("userid") int userid) {
		List<RCProject> projects = ownedByUser(userid);
		RCWorkspaceQueries wspaceDao = getHandle().attach(RCWorkspaceQueries.class);
		for (RCProject proj: projects) {
			proj.setWorkspaces(wspaceDao.forProjectIncludingFiles(proj.getId()));
		}
		return projects;
	}

}
