package org.nanotek.crawler.data.config;

import javax.persistence.EntityManager;

import org.nanotek.crawler.data.stereotype.EntityBaseRepositoryImpl;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;


public class MyJpaRepositoryFactory extends JpaRepositoryFactory{

	public MyJpaRepositoryFactory(EntityManager entityManager) {
		super(entityManager);
	}


	@Override
	protected ProjectionFactory getProjectionFactory() {
		// TODO Auto-generated method stub
		return super.getProjectionFactory();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.RepositoryFactorySupport#getRepositoryBaseClass(org.springframework.data.repository.core.RepositoryMetadata)
	 */
	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		return EntityBaseRepositoryImpl.class;
	}
}
