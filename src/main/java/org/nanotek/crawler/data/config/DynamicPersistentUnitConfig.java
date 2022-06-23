package org.nanotek.crawler.data.config;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Id;
import javax.persistence.ValidationMode;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceProvider;
import javax.sql.DataSource;

import org.hibernate.cfg.Environment;
import org.nanotek.crawler.data.config.meta.MetaClass;
import org.nanotek.crawler.data.stereotype.EntityBaseRepositoryImpl;
import org.nanotek.crawler.data.util.db.ControllerClassConfig;
import org.nanotek.crawler.data.util.db.JdbcHelper;
import org.nanotek.crawler.data.util.db.PersistenceUnityClassesConfig;
import org.nanotek.crawler.data.util.db.RepositoryClassesConfig;
import org.nanotek.crawler.data.util.db.SimpleObjectProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.support.MergingPersistenceUnitManager;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import net.bytebuddy.dynamic.loading.InjectionClassLoader;
import net.bytebuddy.dynamic.loading.MultipleParentClassLoader;

@SpringBootConfiguration
@EnableJpaRepositories(
		basePackages = 
	{"org.nanotek.data.entity.mb.buddy.repositories"}
		, transactionManagerRef = "transactionManager")
public class DynamicPersistentUnitConfig implements ApplicationContextAware{

	
	@Bean("classCache")
	@Qualifier(value="classCache")
	public Map<Class<?>,String> classCache(){
		return new HashMap<>();
	}
	
	@Bean
	@Primary
	InjectionClassLoader injectionClassLoader() {
		InjectionClassLoader ic = new  MultipleParentClassLoader(Thread.currentThread().getContextClassLoader() 
				, Arrays.asList(getClass().getClassLoader() , CrudMethodMetadata.class.getClassLoader())  , 
				false);
		return ic;
	}

	@Bean(value = "persistenceUnityClassesConfig")
	@Primary
	public PersistenceUnityClassesConfig persistenceUnityClassesConfig() {
		return new PersistenceUnityClassesConfig();
	}

	@Bean 
	@Primary
	public RepositoryClassesConfig repositoryClassesConfig() {
		return new RepositoryClassesConfig();
	}

	@Primary
	@Bean(name = "defaultDataSourceProperties")
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSourceProperties defaultDataSourceProperties() {
		DataSourceProperties dsp = new DataSourceProperties();
		dsp.setName("portal-data-source-source");
		return dsp;
	}

	@Primary
	@Bean
	public DataSource defaultDataSource(
			@Qualifier("defaultDataSourceProperties") DataSourceProperties dataSourceProperties) {
		return dataSourceProperties.initializeDataSourceBuilder().build();
	}

	@Bean
	@Qualifier(value="myHelper")
	@DependsOn({"jdbcTemplate" , "defaultDataSourceProperties"})
	JdbcHelper myHelper() {
		return new JdbcHelper();
	}


	@Bean
	@Primary
	JdbcTemplate jdbcTemplate(@Autowired DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	@Primary
	@DependsOn("jdbcTemplate")
	JdbcHelper helper() {
		return new JdbcHelper();
	}

	
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean 
	@Qualifier(value="myBf")
	public DefaultListableBeanFactory defaultListableBeanFactory(@Autowired InjectionClassLoader classLoader )
	{
		DefaultListableBeanFactory v = new DefaultListableBeanFactory();
		v.setParentBeanFactory(context);
		v.setBeanClassLoader(classLoader);
		return v;
	}
	ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.context = applicationContext;
	}

	@Bean
	@Qualifier(value="buddyJpaPropertie")
	public Map<String, Object> buddyJpaPropertie(){
		Map<String, Object> jpaPropertiesMap = new HashMap<>();
		jpaPropertiesMap.put(Environment.FORMAT_SQL, true);
		jpaPropertiesMap.put(Environment.SHOW_SQL, true);
		jpaPropertiesMap.put("hibernate.globally_quoted_identifiers", false);
		jpaPropertiesMap.put("hibernate.transaction.auto_close_session" , true);
		jpaPropertiesMap.put("hibernate.current_session_context_class" , "thread" );
		jpaPropertiesMap.put("hibermate.hbm2ddl.auto" , "none" );
		return jpaPropertiesMap;
	}

	public Integer configureClasses(@Autowired InjectionClassLoader injectionClassLoader , 
			@Autowired @Qualifier("myHelper") JdbcHelper helper, 
			@Autowired RepositoryClassesConfig repoConfig, 
			@Autowired  PersistenceUnityClassesConfig persistenceUnityClassesConfig, 
			@Autowired DefaultListableBeanFactory defaultListableBeanFactory
			) throws Exception {
		List <MetaClass> classMap  = helper.getClassMaps();

		classMap 
		.stream().filter(cm -> cm.isHasPrimeraryKey())
		.filter(cm -> !cm.getClass().equals(java.sql.RowId.class))
		.forEach(cm ->{
			try {
				Class<?> theClass;
				try {
					theClass = helper.generateBaseClass(cm, injectionClassLoader);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				Optional.ofNullable(theClass)
				.ifPresent((clazz1)-> {
					persistenceUnityClassesConfig.put(clazz1.getSimpleName(), clazz1);
					persistenceUnityClassesConfig.getTypeCache().insert(injectionClassLoader, theClass.getSimpleName(), clazz1);
					Optional<Class<?>> idClass = getIdClass(clazz1);
					idClass.ifPresent(idc -> {
						Class<?> repClass = repoConfig.prepareReppositoryForClass(clazz1, idc, injectionClassLoader)	;
						repoConfig.put(clazz1.getSimpleName(), repClass);
						ControllerClassConfig ccc = controllerClassConfig();
						try {
							Class<?>  controllerClass = ccc.createControllerClass(cm , repClass, clazz1, idClass.get(), injectionClassLoader);
							ccc.addControllerClass(controllerClass);
						} catch (Exception e) {
							e.printStackTrace();
							throw new RuntimeException(e);
						}
				});
				//
//				if (persistenceUnityClassesConfig.getOrDefault(theClass.getSimpleName() , null) == null) {

				});
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		});
		return Integer.MAX_VALUE;
	}
	
	@Bean
	public ControllerClassConfig controllerClassConfig() {
		return new ControllerClassConfig();
	}

	private Optional<Class<?>> getIdClass(Class<?> theClass) {
		Class<?> idClass = null;
		for (Field f :theClass.getDeclaredFields()) {
			Id id = f.getAnnotation(Id.class);
			if (id !=null) {
				idClass = f.getType();
				break;
			}
		}
		return Optional.ofNullable(idClass);
	}

	@Bean(name = "entityManagerFactory")
	@Qualifier(value="entityManagerFactory")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Autowired DataSource dataSource ,
			@Autowired  @Qualifier("myPersistenceManager") MergingPersistenceUnitManager myPersistenceManager , 
			@Autowired InjectionClassLoader injectionClassLoader , 
			@Autowired @Qualifier("myHelper") JdbcHelper helper, 
			@Autowired RepositoryClassesConfig repoConfig, 
			@Autowired  PersistenceUnityClassesConfig persistenceUnityClassesConfig, 
			@Autowired Initializer initializer  ) throws Exception {
		configureClasses(injectionClassLoader , helper , repoConfig , persistenceUnityClassesConfig , defaultListableBeanFactory(injectionClassLoader));
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setGenerateDdl(false);
		MyEntityFactoryBean factory = new MyEntityFactoryBean(injectionClassLoader);
		factory.setPersistenceUnitManager(myPersistenceManager);
		factory.setJpaVendorAdapter(vendorAdapter);
		factory.setDataSource(dataSource);
		factory.setBeanClassLoader(injectionClassLoader);
		factory.setPersistenceProviderClass(SpringHibernateJpaPersistenceProvider.class);
		factory.setJpaVendorAdapter(new JpaVendorAdapter() {
			@Override
			public PersistenceProvider getPersistenceProvider() {
				return new SpringHibernateJpaPersistenceProvider(injectionClassLoader , persistenceUnityClassesConfig);
			}});
		factory.setEntityManagerInitializer(initializer);
		factory.setConfig(persistenceUnityClassesConfig);
		factory.setJpaPropertyMap(buddyJpaPropertie());
		factory.setPersistenceUnitName("buddyPU");
		factory.afterPropertiesSet2();
		return factory;
	}



	@Bean(name = "RepositoryControllerPreparation")
	@DependsOn("entityManagerFactory")
	Integer prepareRepositories( @Autowired  EntityManagerFactory entityManagerFactory , 
			@Autowired DefaultListableBeanFactory defaultListableBeanFactory , 
			@Autowired RepositoryClassesConfig config, 
			@Autowired InjectionClassLoader classLoader,
			@Autowired PersistenceUnityClassesConfig classConfig){
		defaultListableBeanFactory.setBeanClassLoader(classLoader);
		System.out.println("this is the place");
		//		defaultListableBeanFactory.getBean("AreaRepository");
		config
		.forEach((x , repClass) ->{
			try {
				String sName = repClass.getSimpleName().replace("Repository", "");
				String sNmae2 = sName; // "org.nanotek.data.entity.mb."+
				Optional.ofNullable(classConfig.getOrDefault(sNmae2 , null))
				.ifPresentOrElse(c ->{
					//					    MyJpaRepositoryComponentBean fv = new MyJpaRepositoryComponentBean(repClass);
					//		 	fv.setEntityPathResolver(new SimpleObjectProvider(pr));
					//		 	fv.setBeanClassLoader(injectionClassLoader);
					//		    fv.set
					configureRepositoryBean(defaultListableBeanFactory , c , sNmae2 , repClass , classLoader);
					configureControllerBean(defaultListableBeanFactory );
				}, () -> {
					System.out.println("Entity Class Not Found on Entity Cache");
				});

			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException (e);
			}
		});
		return Integer.MAX_VALUE;
	}

	
	private void configureControllerBean(DefaultListableBeanFactory defaultListableBeanFactory) {
		
		controllerClassConfig()
		.getControllerClasses()
		.stream()
		.forEach(cc -> {
			GenericBeanDefinition bd = new GenericBeanDefinition();
			bd.setBeanClass(cc);
			bd.setLazyInit(true);
			if (!defaultListableBeanFactory.containsBean(cc.getSimpleName()))
					defaultListableBeanFactory.registerBeanDefinition(cc.getSimpleName(), bd);
		});
		
		
	}
	
	private void configureRepositoryBean(DefaultListableBeanFactory defaultListableBeanFactory, Class<?> c,
			String sNmae2, Class<?> repClass, InjectionClassLoader classLoader) {
		SimpleEntityPathResolver pr = new SimpleEntityPathResolver(sNmae2);
		GenericBeanDefinition bd = new GenericBeanDefinition();
		bd.setBeanClass(MyJpaRepositoryComponentBean.class);
		bd.setLazyInit(true);
		ConstructorArgumentValues cav = new ConstructorArgumentValues();
		cav.addGenericArgumentValue(new ValueHolder(repClass));
		bd.setConstructorArgumentValues(cav);

		bd.setPropertyValues(new MutablePropertyValues().add("entityPathResolver", 
				new SimpleObjectProvider<>(pr))
				.add("beanClassLoader", classLoader)
				.add("beanFactory", defaultListableBeanFactory)
				.add("repositoryBaseClass", EntityBaseRepositoryImpl.class)
				.add("entityClass", c));

		bd.addQualifier(new AutowireCandidateQualifier(repClass.getSimpleName()));
		defaultListableBeanFactory.registerBeanDefinition(repClass.getSimpleName(), bd);
		
	}

	@Bean
	Initializer initializer(){
		return new Initializer();
	}

	class Initializer implements Consumer<EntityManager>{

		@Autowired
		PersistenceUnityClassesConfig config;

		@SuppressWarnings("unused")
		@Override
		public void accept(EntityManager em) {
			Metamodel model = em.getMetamodel();
		}

	}

	@Bean(value="myPersistenceManager")
	@Qualifier(value="myPersistenceManager")
	public MergingPersistenceUnitManager myPersistenceManager(@Autowired DataSource dataSource ) {
		MergingPersistenceUnitManager pum = new  MyMergingPersistenceUnitManager();
		pum.setValidationMode(ValidationMode.NONE);
		pum.setDefaultPersistenceUnitName("buddyPU");
		pum.setPackagesToScan("org.nanotek.data.entity.mb");
		pum.setDefaultDataSource(dataSource);
		pum.setPersistenceUnitPostProcessors(myProcessor());
		pum.preparePersistenceUnitInfos();
		return pum;
	}

	@Bean
	public PersistenceUnitPostProcessor myProcessor () {
		return new MyPersistenceUnitPostProcessor();
	}

	class MyPersistenceUnitPostProcessor  implements PersistenceUnitPostProcessor{

		@Autowired
		PersistenceUnityClassesConfig persistenceUnityClassesConfig;

		@Autowired
		RepositoryClassesConfig repositoryClassesConfig;

		@Autowired
		@Qualifier("myHelper")
		JdbcHelper helper;

		@Autowired
		@Qualifier("myBf")
		DefaultListableBeanFactory defaultListableBeanFactory;

		@Autowired
		InjectionClassLoader classLoader;

		@Override
		public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
			defaultListableBeanFactory.setBeanClassLoader(classLoader);
			repositoryClassesConfig
			.forEach((x,y)->{
				pui.addManagedClassName(y.getName());
				Class<?> clazz = Class.class.cast(y); 
				ConstructorArgumentValues cav = new ConstructorArgumentValues();
				cav.addGenericArgumentValue(new ValueHolder(clazz));
				AnnotatedGenericBeanDefinition bd = new AnnotatedGenericBeanDefinition(MyJpaRepositoryComponentBean.class);
				bd.setLazyInit(true);
				bd.setConstructorArgumentValues(cav);	
				System.err.println("The Bean Name " + clazz.getSimpleName());
				//				
				//			  if (!(	defaultListableBeanFactory.containsBean(clazz.getSimpleName()) || defaultListableBeanFactory.containsBeanDefinition(clazz.getSimpleName()))) {
				//				defaultListableBeanFactory.registerBeanDefinition(clazz.getSimpleName(), bd);
				//			  }
			});
			pui.addManagedPackage("org.nanotek.data.entity.mb");
			pui.setValidationMode(ValidationMode.NONE);
			pui.setExcludeUnlistedClasses(false);
			Properties p = new Properties(); 
			buddyJpaPropertie().entrySet().stream().forEach(e -> p.put(e.getKey(), e.getValue().toString()));
			pui.setProperties(p);
			pui.setPersistenceUnitName("buddyPU");
		}
	}

	public void prepareRepository(Class <?> entity, RepositoryClassesConfig config )
	{
		Class<?> repo = config 
				.prepareReppositoryForClass(entity, Long.class , injectionClassLoader() );
		config.put(entity.getSimpleName(), repo);
	}

	@Bean("transactionManager")
	@Qualifier(value="transactionManager")
	public PlatformTransactionManager defaultTransactionManager(
			@Autowired	@Qualifier("entityManagerFactory") EntityManagerFactory factory) {
		return new JpaTransactionManager(factory);
	}

}
