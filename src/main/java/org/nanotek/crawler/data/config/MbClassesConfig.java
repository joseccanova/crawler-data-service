package org.nanotek.crawler.data.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.nanotek.crawler.Base;
import org.nanotek.crawler.data.config.meta.Id;
import org.nanotek.crawler.data.config.meta.MetaClass;
import org.nanotek.crawler.data.config.meta.MetaDataAttribute;
import org.nanotek.crawler.data.util.Holder;
import org.nanotek.crawler.data.util.db.JdbcHelper;
import org.nanotek.crawler.data.util.graph.actuator.mappings.mb.DispatcherServlet;
import org.nanotek.crawler.data.util.graph.actuator.mappings.mb.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;

@Slf4j
@SpringBootConfiguration
public class MbClassesConfig {


	public static final String PACKAGE = "org.nanotek.entity.mb.";
	public final String PORTAL_PACKAGE = "org.nanotek.entity.data";

	@Autowired
	@Lazy(true)
	RestTemplate restTemplate; 

	@Autowired
	@Lazy(true)
	ObjectMapper objectMapper;

	@Autowired
	@Lazy(true)
	EurekaClient eurekaClient;
	
	Optional<Root> rootMapping ;

	@Autowired
	DefaultListableBeanFactory beanFactory;

	private Map<String, String> urlBaseMapping;

	
	@Bean
	@Qualifier(value="acxClassCache")
	@Lazy(true)
	public Map<Class<?> , String> acxClassCache(){
		try {
			return processMetaClasses(getMappings());
		}catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public Optional<Root> getActuatorMappings() {
		
		InstanceInfo info =  eurekaClient.getNextServerFromEureka("acx-data-service", false);
		String uri =  info.getHomePageUrl() + "actuator/mappings";
		ResponseEntity<Root> paramMappings=restTemplate.getForEntity(uri, Root.class);
		rootMapping = Optional.ofNullable( paramMappings.getBody());
		return rootMapping;
	}
	
	public Map<String,String> getMappings(){
		Map<String , String> theMap = new HashMap<>();
		getActuatorMappings().ifPresent(r ->{
			List<DispatcherServlet> ms = 
					r.contexts.mbDataService.mappings.dispatcherServlets.dispatcherServlet;
			ms.
			stream()
			.map(ds -> getHandlersMethods(ds))
			.filter(e -> e.isPresent())
			.map(e ->e.get())
			.forEach(e -> putEntry(e , theMap));
		});
		return theMap;
	}
	
	private void putEntry(Entry<?, ?> e, Map<String, String> theMap) {
		theMap.put(e.getKey().toString(), e.getValue().toString());
	}
	
	private Optional< Map.Entry<?, ?>> getHandlersMethods(DispatcherServlet ds) {
		Holder<Map.Entry<?, ?>> oe = new Holder<>();
		if (ds.handler.contains(PORTAL_PACKAGE)) {
			String pattern =  ds.details.requestMappingConditions.patterns.get(0);
			if (pattern.contains("search"))
			{
				String handler = ds.handler.replaceAll(PORTAL_PACKAGE, "");
				handler = handler.substring(1 , handler.indexOf("#"));
				String theHandler = handler.replaceAll("Controller" , "");
				oe.put(Map.entry(theHandler, pattern));
			}
		}
		return oe.get();
	}
	
	
	public Map<Class<?>, String> processMetaClasses(Map<String, String> urlBaseMapping) {
		Map<Class<?> , String> acxClassCache = new HashMap<>();
		this.urlBaseMapping = urlBaseMapping;
		InstanceInfo info =  eurekaClient.getNextServerFromEureka("crawler-data-service-acx", false);
		urlBaseMapping
		.forEach((x , y)->{
			Path p = Paths.get(y, new String[0]);
			Path p1 = p.getParent();
			String uri =  info.getHomePageUrl() +  p1.getName(0) + "/metaclass";
			String theMetaResponse = restTemplate.getForObject(uri, String.class);
			MetaClass clazz;
			try {
				clazz = objectMapper.readValue(theMetaResponse, MetaClass.class);
				Class<?> base = proccessMetaClass(clazz);
				acxClassCache.put(base ,  info.getHomePageUrl() +  p1.getName(0));
			} catch (Exception e) {
				log.info("error" , e);
				throw new RuntimeException(e);
			}
		} );
		
		return acxClassCache;
	}
	
	
	
	private Class<?> proccessMetaClass(MetaClass body) throws ClassNotFoundException {
		fixPrimaryKeyClass(body);
		Class<?> clazz = createBaseClass(body, beanFactory.getBeanClassLoader());
		return clazz;
	}

	private void fixPrimaryKeyClass(MetaClass body) {
		if(body.getMetaAttributes()
		.stream()
		.filter(a -> a.isId())
		.count()>1) { 
			int i = 0;
			for (MetaDataAttribute att : body.getMetaAttributes()) {
				if (att.isId()) {
					i++;
					if (i > 1) {
						att.setId(false);
					}
				}
			}
		}
	}

	private <T> Class<?> createBaseClass(MetaClass cm, ClassLoader classLoader) {
		
		Class<?> baseClass =  Optional.of(cm).filter(cm1 -> cm1.getMetaAttributes().stream().anyMatch(cm11 -> cm11.isId()))
				.map(cm11 ->{
					List<MetaDataAttribute> metaAttributes = cm11.getMetaAttributes();
					Builder<T> bd = processClassMetaData (cm , classLoader);
					Holder<Builder<T>> h = new Holder<>();
					h.put(bd);
					metaAttributes
					.stream()
					.forEach(m -> {
						processMetaAttribute(m , h);
					});
					String myClassName =  normalizeClassName(cm11.getClassName());
					Class<?> c = createBuddyClass(h , myClassName , classLoader);
					return c;
				}).orElse(null);
		return baseClass;
	}

	@Autowired 
	JdbcHelper helper;
	
	private String normalizeClassName(String className) {
		String myClassName =  helper.processNameTranslationStrategy(className);
		myClassName =  JdbcHelper.snakeToCamel(myClassName);
		return myClassName;
	}


	private <T> Class<?> createBuddyClass(Holder<Builder<T>> h, String myClassName, ClassLoader classLoader) {
		Builder<T> theBuilder = h.get().orElseThrow();
		Class<?> c = theBuilder.make().load(classLoader).getLoaded();
		return c;
	}
	private  <T>  void processMetaAttribute(MetaDataAttribute m , Holder<Builder<T>> h) {
		try {
			String fieldName = m.getFieldName();
			final String thefinalFieldName  = helper.processNameTranslationStrategy(fieldName);
			final String thefinalFieldName1  = helper.snakeToCamel(thefinalFieldName);
			Class<?> ca = m.getClazz();
			Builder<T> bd1  = Optional
					.ofNullable(m)
					.filter(f -> f.isId())
					.map(fname -> h.get().get() .defineProperty(thefinalFieldName1.trim(), ca).annotateField(AnnotationDescription.Builder.ofType(Id.class).build()))
					.orElse(h.get().get().defineProperty(thefinalFieldName1.trim(), ca));
			h.put(bd1);
		} catch (Exception e1) {
			log.info("error" , e1);
			throw new RuntimeException(e1);
		}		
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> Builder<T> processClassMetaData(MetaClass cm11, ClassLoader classLoader) {
		String classNameCandidate = cm11.getClassName();
		String myClassName =  JdbcHelper.prepareName(helper.processNameTranslationStrategy(classNameCandidate));
		AnnotationDescription toString =  AnnotationDescription.Builder.ofType(ToString.class)
				.build();
		AnnotationDescription jsonMappingAnnotation =  AnnotationDescription.Builder.ofType(JsonNaming.class)
		.define("value", PropertyNamingStrategies.LowerCamelCaseStrategy.class)
		.build();

		Builder bd = new ByteBuddy(ClassFileVersion.JAVA_V8)
				.subclass(Base.class)
				.name(PACKAGE+myClassName)
				.annotateType(jsonMappingAnnotation)
				.annotateType(toString)
				.withHashCodeEquals()
				.withToString()
				.defineProperty("metaClass", MetaClass.class)
				.annotateField(AnnotationDescription.Builder.ofType(JsonIgnore.class).build());
		return bd;
	}
	
	public Optional<Root> getRootMapping() {
		return rootMapping;
	}

	public Map<String, String> getUrlBaseMapping() {
		return urlBaseMapping;
	}

	public Map<Class<?>, String> getClassCache() {
		return acxClassCache();
	}

}
