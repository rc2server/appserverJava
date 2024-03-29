package edu.wvu.stat.rc2.persistence;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wvu.stat.rc2.jdbi.BindFile;
import edu.wvu.stat.rc2.jdbi.BindInputStream;
import edu.wvu.stat.rc2.persistence.RCFile.RCFileMapper;

public abstract class RCFileQueries {
	final static Logger log = LoggerFactory.getLogger("rc2.RCFileQueries");

	@SqlQuery("select * from rcfile where id = :id")
	@Mapper(RCFileMapper.class)
	public abstract RCFile findById(@Bind("id") int id);
	
	@SqlQuery("select * from rcfile where wspaceid = :wspaceId")
	@Mapper(RCFileMapper.class)
	public abstract List<RCFile> filesForWorkspaceId(@Bind("wspaceId") int wspaceID);
	
	@SqlQuery("select bindata from rcfiledata where id = :id")
	public abstract byte[] fileDataById(@Bind("id") int id);
	
	@SqlUpdate("delete from rcfile where id = :id")
	@Mapper(RCFileMapper.class)
	public abstract int deleteFile(@Bind("id") int id);
	

	@SqlUpdate("insert into rcfile (wspaceId, name, fileSize) values (:wspaceId, :name, :fileSize)")
	@GetGeneratedKeys
	abstract int createFile(@Bind("wspaceId") int wspaceId, @Bind("name") String name, @Bind("fileSize") int fileSize);
	
	@SqlUpdate("insert into rcfiledata (id, bindata) values (:id, :file) returning length(bindata)")
	abstract int createFileData(@Bind("id") int id, @BindFile("file") File inputFile);

	@SqlUpdate("update rcfiledata set bindata = :stream where id = :id")
	abstract int updateFileData(@Bind("id") int id, @BindInputStream("stream") InputStream stream);
	
	@SqlUpdate("update rcfile set version = version + 1, lastmodified = now() where id = :id")
	abstract int updateFileVersion(@Bind("id") int id);

	@SqlUpdate("update rcfile set name = :name, version = version + 1, lastmodified = now() where id = :id")
	abstract int updateName(@Bind("id") int id, @Bind("name") String name);

	@SqlUpdate("insert into rcfiledata (id, bindata) select :destId, bindata from rcfiledata where id = :srcId")
	abstract int copyFileData(@Bind("srcId") int srcId, @Bind("destId") int destId);
	
	@Transaction
	public RCFile updateFileName(int fileId, String newName) {
		try {
			if (updateName(fileId, newName) == 1)
				return findById(fileId);
		} catch (Exception e) {
			log.error("error updating file name", e);
		}
		return null;
	}
	
	@Transaction
	public RCFile updateFileContents(int fileId, InputStream contents) {
		try {
			updateFileData(fileId, contents);
			updateFileVersion(fileId);
		} catch (Exception e) {
			log.error("error updating file contents", e);
			return null;
		}
		return findById(fileId);
	}
	
	@Transaction
	public RCFile duplicateFile(RCFile file, String newName) {
		int fileId = 0;
		try {
			fileId = createFile(file.getWspaceId(), newName, file.getFileSize());
			copyFileData(file.getId(), fileId);
		} catch (Exception e) {
			log.error("error duplicating file", e);
			return null;
		}
		return findById(fileId);
	}
	
	@Transaction
	public RCFile createFile(int wspaceId, String name, File file) {
		int fileId = 0;
		try {
			fileId = createFile(wspaceId, name, (int)file.length());
			createFileData(fileId, file);
		} catch (Exception e) {
			log.error("error creating an RCFile", e);
		}
		return findById(fileId);
	}

	@Transaction
	public RCFile createFileWithStream(int wspaceId, String name, InputStream stream) {
		try {
			final Path tmpFile = Files.createTempFile("rc2su", ".tmp");
			try {
				Files.copy(stream, tmpFile, StandardCopyOption.REPLACE_EXISTING);
				return createFile(wspaceId, name, tmpFile.toFile());
			} finally {
				Files.delete(tmpFile);
			}
		} catch (Exception e) {
			log.error("error creating an RCFile", e);
		}
		return null;
	}

}
