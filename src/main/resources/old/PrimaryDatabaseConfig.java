package org.nanotek.data.portal.data.config;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.cfg.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.nanotek.data.util.MapBeanTransformer;

@SpringBootConfiguration
@EnableTransactionManagement
@ComponentScan({"org.nanotek.data"}  )
@ConfigurationProperties
@EnableJpaRepositories(
		basePackages = 
	{"org.nanotek.data.repositories"}
					, transactionManagerRef = "defaultTransactionManager")
@EnableAutoConfiguration(exclude = 
						{ 
						HibernateJpaAutoConfiguration.class})
public class PrimaryDatabaseConfig {

	@Bean 
	@DependsOn(value = "objectMapper")
	public MapBeanTransformer<Object> mapBean(){
		return new MapBeanTransformer<Object> ();
	}
	
	@Bean
	@Primary
	@Qualifier(value="customjpaproperties")
	public Map<String, Object> customjpaproperties(){
		Map<String, Object> jpaPropertiesMap = new HashMap<>();
		jpaPropertiesMap.put(Environment.FORMAT_SQL, true);
		jpaPropertiesMap.put(Environment.SHOW_SQL, true);
		jpaPropertiesMap.put("hibernate.globally_quoted_identifiers", false);
		jpaPropertiesMap.put("hibernate.dialect", "org.hibernate.dialect.Oracle10gDialect");
		jpaPropertiesMap.put("hibernate.transaction.auto_close_session" , true);
		jpaPropertiesMap.put("hibernate.current_session_context_class" , "thread" );
		jpaPropertiesMap.put("hibermate.hbm2ddl.auto" , "none" );
		return jpaPropertiesMap;
	}


	@Bean(name = "entityManagerFactory")
	@Primary
	@Qualifier(value="entityManagerFactory")
	@DependsOn(value = "dataSource")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Autowired DataSource dataSource) {
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setGenerateDdl(false);
		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setJpaVendorAdapter(vendorAdapter);
		factory.setPackagesToScan(new String []{"org.nanotek.data.domain"});
		factory.setPersistenceUnitName("oracleDefault");
		factory.setJpaPropertyMap(customjpaproperties());
		factory.setDataSource(dataSource);
		return factory;

	}

	@Primary
	@Bean
	public PlatformTransactionManager defaultTransactionManager(
			@Qualifier("entityManagerFactory") EntityManagerFactory factory) {
		return new JpaTransactionManager(factory);
	}

}
