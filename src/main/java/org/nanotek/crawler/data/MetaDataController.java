package org.nanotek.crawler.data;

import org.nanotek.crawler.BaseEntity;
import org.nanotek.crawler.data.stereotype.EntityBaseRepository;
import org.nanotek.crawler.data.util.db.PersistenceUnityClassesConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MetaDataController
<T extends BaseEntity<ID> , ID , R extends EntityBaseRepository<T,ID> > 
implements BaseEntityController<T, ID, R >  {

	@Autowired
	@Qualifier("persistenceUnityClassesConfig")
	PersistenceUnityClassesConfig classesConfig;

	@Autowired
	@Lazy(true)
	R repository;
	
	@Autowired
	@Lazy(true)
	@Qualifier("myObjectMapper")
	ObjectMapper objectMapper;

	@Override
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public MetaDataController() {

	}

	@Override
	public R getRepository() {
		return repository;
	}

	public PersistenceUnityClassesConfig getClassesConfig() {
		return classesConfig;
	}

	public void setClassesConfig(PersistenceUnityClassesConfig classesConfig) {
		this.classesConfig = classesConfig;
	}

	public void setRepository(R repository) {
		this.repository = repository;
	}

}
