package edu.wvu.stat.rc2.persistence;

import java.util.Date;
import com.google.auto.value.AutoValue;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@AutoValue
public abstract class RCFile implements PersistentObject {
	
	static RCFile create(int id, String name, int version, Date dateCreated, Date lastModified, int fileSize) {
		return new AutoValue_RCFile(id, name, version, dateCreated, lastModified, fileSize);
	}

	abstract int id();
	abstract String name();
	abstract int version();
	abstract Date dateCreated();
	abstract Date lastModified();
	abstract int fileSize();
	
}
