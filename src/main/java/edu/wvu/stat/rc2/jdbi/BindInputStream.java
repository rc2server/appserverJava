package edu.wvu.stat.rc2.jdbi;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;

/** this annotation allows binding an input stream to a jdbi sqlquery */

@BindingAnnotation(BindInputStream.InputStreamBinderFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface BindInputStream {
	String value();
	public static class InputStreamBinderFactory implements BinderFactory {
		public Binder<BindInputStream, InputStream> build(Annotation annotation) {
			return new Binder<BindInputStream, InputStream>() {
				public void bind(SQLStatement<?> q, BindInputStream bind, InputStream stream) {
					try {
						final File tmpFile = File.createTempFile("rc2upload", ".tmp");
						try {
							final Path destination = Paths.get(tmpFile.toURI());
							Files.copy(stream, destination);
							q.bindBinaryStream(bind.value(), new FileInputStream(tmpFile), (int) tmpFile.length());
						} finally {
							tmpFile.delete();
						}
					} catch (Exception e) {
						//should not be possible
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
			};
		}
	}
}
