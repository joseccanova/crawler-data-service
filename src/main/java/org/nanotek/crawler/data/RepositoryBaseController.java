package org.nanotek.crawler.data;

import org.nanotek.crawler.BaseEntity;
import org.nanotek.crawler.data.util.db.JdbcHelper;
import org.nanotek.crawler.data.util.db.PersistenceUnityClassesConfig;

public interface RepositoryBaseController<T , B extends BaseEntity<?>> {
	T getRepository();
	PersistenceUnityClassesConfig getClassesConfig();
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	default B prepareClass(String entityName) {
		try {
			String clstr = entityName;
			Class<B> tclass = Class.class.cast(getClassesConfig().get(clstr));
			return tclass.newInstance();
		}
		catch (Exception e) {
			try {
				String clstr = JdbcHelper.PACKAGE+entityName;
				Class<B> tclass = Class.class.cast(getClassesConfig().get(clstr));
				return tclass.newInstance();
			}
			catch(Exception e1) {
				throw new RuntimeException(e1);
			}
		}
	}
}
