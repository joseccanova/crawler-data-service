package org.nanotek.crawler.data.config;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.nanotek.crawler.data.util.db.PersistenceUnityClassesConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module.Feature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.utils.jackson.RegistrationDeserializer;
import net.bytebuddy.dynamic.loading.InjectionClassLoader;

@SpringBootConfiguration
public class FasterXmlConfiguration  {
	
	
	@Bean(value="myObjectMapper")
	@Qualifier (value="myObjectMapper")
	public ObjectMapper objectMapper() {
		DateTimeFormatter dtf =  DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		LocalDateTimeSerializer serializer = new LocalDateTimeSerializer(dtf);
	    JavaTimeModule module = new JavaTimeModule();
	    Hibernate5Module hibModule = new Hibernate5Module();
	    hibModule.enable(Feature.REPLACE_PERSISTENT_COLLECTIONS);
	    hibModule.enable(Feature.WRITE_MISSING_ENTITIES_AS_NULL);
	    hibModule.enable(Feature.REQUIRE_EXPLICIT_LAZY_LOADING_MARKER);
	    module.addSerializer(serializer);
	    ObjectMapper obj = new ObjectMapper()
	      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
	      .registerModule(module)
	      .registerModule(hibModule);
	    return obj;
	}
	
	@Bean
	@Primary
	@DependsOn("entityManagerFactory")
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter
				(@Autowired @Qualifier("myObjectMapper") ObjectMapper obj , @Autowired PersistenceUnityClassesConfig classesConfig) {
		MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
		List<MediaType> mediaTypes = new ArrayList<>();
		mediaTypes.add(MediaType.APPLICATION_JSON);
		mediaTypes.add(MediaType.TEXT_HTML);
		mediaTypes.add(MediaType.APPLICATION_XML);
		mediaTypes.add(MediaType.TEXT_XML);
		mediaTypes.add(new MediaType("application", "*+xml"));
		mediaTypes.add(MediaType.ALL);
		jsonConverter.setSupportedMediaTypes(mediaTypes);

		objectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL); // objectMapper refers to your own custom mapper
		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
		simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
		simpleModule.addSerializer(Registration.class, ToStringSerializer.instance);
		simpleModule.addDeserializer(Registration.class, new RegistrationDeserializer());
		objectMapper().registerModule(simpleModule);
		jsonConverter.setObjectMapper(obj);	    
		classesConfig.forEach((x,y) ->{
		    jsonConverter.registerObjectMappersForType(y, registar ->{
		    			registar.putIfAbsent(MediaType.APPLICATION_JSON, obj);
		    });
    	});
	    jsonConverter.registerObjectMappersForType(Registration.class, registar ->{
			registar.putIfAbsent(MediaType.APPLICATION_JSON, obj);
	    });
	    return jsonConverter;
	}
	
	@Bean
	@Primary
	public Jackson2ObjectMapperFactoryBean mapperFactoryBean(	@Autowired
			InjectionClassLoader classLoader,
			@Autowired
			DefaultListableBeanFactory beanFactory, @Autowired 	@Qualifier("myObjectMapper") ObjectMapper objectMapper) {
		Jackson2ObjectMapperFactoryBean mapperFactoryBean = new Jackson2ObjectMapperFactoryBean ();
		mapperFactoryBean.setBeanClassLoader(classLoader);
		mapperFactoryBean.setObjectMapper(objectMapper);
		return mapperFactoryBean;
	}
	
//	@Bean
//	public Jackson2ObjectMapperBuilder configureObjectMapper() {
//		DateTimeFormatter dtf =  DateTimeFormatter.ISO_LOCAL_DATE_TIME;
//		LocalDateTimeSerializer serializer = new LocalDateTimeSerializer(dtf);
//	    Hibernate5Module hibModule = new Hibernate5Module();
//	    hibModule.enable(Feature.REPLACE_PERSISTENT_COLLECTIONS);
//	    hibModule.enable(Feature.WRITE_MISSING_ENTITIES_AS_NULL);
//	    hibModule.enable(Feature.REQUIRE_EXPLICIT_LAZY_LOADING_MARKER);
//		JavaTimeModule timeModule = new JavaTimeModule();
//		timeModule.addSerializer(serializer);
//		return new Jackson2ObjectMapperBuilder()
//					.modulesToInstall(Hibernate5Module.class , JavaTimeModule.class)
//					.modules(timeModule,hibModule);
//	}

}
