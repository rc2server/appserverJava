package edu.wvu.stat.rc2.jdbi;

import java.io.File;
import java.io.FileInputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;

/** this annotation allows binding an input stream to a jdbi sqlquery */

@BindingAnnotation(BindFile.FileBinderFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface BindFile {
	String value();
	public static class FileBinderFactory implements BinderFactory {
		public Binder<BindFile, File> build(Annotation annotation) {
			return new Binder<BindFile, File>() {
				public void bind(SQLStatement<?> q, BindFile bind, File file) {
					try {
						q.bindBinaryStream(bind.value(), new FileInputStream(file), (int) file.length());
					} catch (Exception e) {
						//should not be possible
						throw new RuntimeException(e);
					}
				}
			};
		}
	}
}
