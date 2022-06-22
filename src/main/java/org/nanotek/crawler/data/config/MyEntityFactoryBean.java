package org.nanotek.crawler.data.config;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

import org.nanotek.crawler.data.util.db.PersistenceUnityClassesConfig;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.util.ClassUtils;

import net.bytebuddy.dynamic.loading.InjectionClassLoader;

public class MyEntityFactoryBean extends LocalContainerEntityManagerFactoryBean{

	@Autowired 
	InjectionClassLoader  inkectionClassLoader;
	
	@Autowired
	PersistenceUnityClassesConfig persistenceUnityClassesConfig;
	
	String providerClassName;
	
	SpringHibernateJpaPersistenceProvider provider;
	
	public MyEntityFactoryBean(InjectionClassLoader inkectionClassLoader2) {
		this.inkectionClassLoader = inkectionClassLoader2;
	}
	public void afterPropertiesSet2() throws PersistenceException {
		setBeanClassLoader(inkectionClassLoader);
		super.afterPropertiesSet();
	}
	@Override
	public void afterPropertiesSet() throws PersistenceException {
	}
	
	@Override
	public ClassLoader getBeanClassLoader() {
		return inkectionClassLoader ;
	}
	
	@Override
	protected EntityManagerFactory createNativeEntityManagerFactory() throws PersistenceException {

		provider = SpringHibernateJpaPersistenceProvider.class.cast(getPersistenceProvider());
		if (provider == null) {
			this.providerClassName = this.getPersistenceUnitInfo() .getPersistenceProviderClassName();
			if (providerClassName == null) {
				throw new IllegalArgumentException(
						"No PersistenceProvider specified in EntityManagerFactory configuration, " +
						"and chosen PersistenceUnitInfo does not specify a provider class name either");
			}
			Class<?> providerClass = ClassUtils.resolveClassName(providerClassName, getBeanClassLoader());
			provider = (SpringHibernateJpaPersistenceProvider) BeanUtils.instantiateClass(providerClass);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Building JPA container EntityManagerFactory for persistence unit '" +
					this.getPersistenceUnitInfo().getPersistenceUnitName() + "'");
		}
		EntityManagerFactory emf =
				provider.createContainerEntityManagerFactory(this.getPersistenceUnitInfo(), getJpaPropertyMap() , inkectionClassLoader , persistenceUnityClassesConfig);
		postProcessEntityManagerFactory(emf, this.getPersistenceUnitInfo() );
		return emf;
	}
	public void setConfig(PersistenceUnityClassesConfig persistenceUnityClassesConfig2) {
			this.persistenceUnityClassesConfig = persistenceUnityClassesConfig2;
	}
}