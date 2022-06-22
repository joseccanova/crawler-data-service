package org.nanotek.crawler.data.util.db;

import static net.bytebuddy.matcher.ElementMatchers.named;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.Entity;

import org.nanotek.crawler.data.MetaDataController;
import org.nanotek.crawler.data.config.meta.IClass;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.loading.InjectionClassLoader;
import net.bytebuddy.implementation.FixedValue;

public class ControllerClassConfig {

	
	public ControllerClassConfig() {
		super();
	}

	public Class<?> createControllerClass(IClass mc ,   Class<?> repClass, Class<?>clazz , Class<?>idClass , InjectionClassLoader classLoader){
		Generic typeDescription = TypeDescription.Generic.Builder.parameterizedType(MetaDataController.class ,  clazz , idClass ,  repClass ).build().asGenericType();
		Entity theEntity = clazz.getAnnotation(Entity.class);
		Optional.ofNullable(theEntity).orElseThrow();
		Class<?> cd =   new ByteBuddy(ClassFileVersion.JAVA_V8)
				.subclass(typeDescription)
				.name( "org.nanotek.data.data." + theEntity.name() +"Controller")
				.annotateType( AnnotationDescription.Builder.ofType(RestController.class)
						.build())
				.annotateType( AnnotationDescription.Builder.ofType(RequestMapping.class)
						.defineArray("path", new String[] { "/" + theEntity.name().toLowerCase()})
						.defineArray("produces", new String [] {MediaType.APPLICATION_JSON_VALUE})
						.build())
				.method(named("getClazz")).intercept(FixedValue.value(clazz))
				.method(named("getMetaClass")).intercept(FixedValue.value(mc))
				.make()
				.load(classLoader).getLoaded();
		System.out.println(cd.toGenericString());
		return cd;
	}

	
	private List<Class<?>> controllerClasses = new ArrayList<>();

	public void addControllerClass(Class<?> controllerClass) {
		this.controllerClasses.add(controllerClass);
	}

	public List<Class<?>> getControllerClasses() {
		return controllerClasses;
	}

	public void setControllerClasses(List<Class<?>> controllerClasses) {
		this.controllerClasses = controllerClasses;
	}

	
	
}
