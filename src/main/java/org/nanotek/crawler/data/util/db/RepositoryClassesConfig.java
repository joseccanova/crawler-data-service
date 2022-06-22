package org.nanotek.crawler.data.util.db;

import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.persistence.Entity;

import org.nanotek.crawler.data.stereotype.EntityBaseRepository;
import org.nanotek.crawler.data.util.Cache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.stereotype.Repository;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.loading.InjectionClassLoader;

public class RepositoryClassesConfig extends Cache<String, Class<?>> {

	private ConcurrentHashMap<String, Class<?>> entityStore;

	public RepositoryClassesConfig(){
		super(Kind.STRONG , Kind.STRONG);
		constructMap() ;
	}

	private void constructMap() {
		entityStore = new ConcurrentHashMap<>(1000);
	}

	public RepositoryClassesConfig(Kind keyKind, Kind valueKind, boolean identity) {
		super(keyKind, valueKind, identity);
		constructMap() ;
	}

	public RepositoryClassesConfig(Kind keyKind, Kind valueKind) {
		super(keyKind, valueKind);
		constructMap() ;
	}

	@Override
	public Class<?> create(String key) {
		return Optional.ofNullable(entityStore.get(key)).orElseThrow();
	}


	public Class<?> put(String key, Class<?> value) {
		return entityStore.put(key, value);
	}


	public KeySetView<String, Class<?>> keySet() {
		return entityStore.keySet();
	}

	public boolean remove(Object key, Object value) {
		return entityStore.remove(key, value);
	}

	public boolean replace(String key, Class<?> oldValue, Class<?> newValue) {
		return entityStore.replace(key, oldValue, newValue);
	}

	public Class<?> replace(String key, Class<?> value) {
		return entityStore.replace(key, value);
	}

	public Class<?> getOrDefault(Object key, Class<?> defaultValue) {
		return entityStore.getOrDefault(key, defaultValue);
	}

	public void forEach(BiConsumer<? super String, ? super Class<?>> action) {
		entityStore.forEach(action);
	}

	public void replaceAll(BiFunction<? super String, ? super Class<?>, ? extends Class<?>> function) {
		entityStore.replaceAll(function);
	}

	public Enumeration<String> keys() {
		return entityStore.keys();
	}

	public KeySetView<String, Class<?>> keySet(Class<?> mappedValue) {
		return entityStore.keySet(mappedValue);
	}

	public <U> U search(long parallelismThreshold,
			BiFunction<? super String, ? super Class<?>, ? extends U> searchFunction) {
		return entityStore.search(parallelismThreshold, searchFunction);
	}

	public <U> U searchKeys(long parallelismThreshold, Function<? super String, ? extends U> searchFunction) {
		return entityStore.searchKeys(parallelismThreshold, searchFunction);
	}

	public <U> U searchValues(long parallelismThreshold, Function<? super Class<?>, ? extends U> searchFunction) {
		return entityStore.searchValues(parallelismThreshold, searchFunction);
	}

	public <U> U searchEntries(long parallelismThreshold,
			Function<Entry<String, Class<?>>, ? extends U> searchFunction) {
		return entityStore.searchEntries(parallelismThreshold, searchFunction);
	}

	public Class<?> prepareReppositoryForClass(Class<?> clazz , Class<?> idClass, ClassLoader classLoader){
		Generic typeDescription = TypeDescription.Generic.Builder.parameterizedType( EntityBaseRepository.class, clazz , idClass).build().asGenericType();
		Entity theEntity = clazz.getAnnotation(Entity.class);
		Optional.ofNullable(theEntity).orElseThrow();
		Class<?> cd =   new ByteBuddy(ClassFileVersion.JAVA_V8)
//				.makeInterface(EntityBaseRepository.class)
				.makeInterface(typeDescription)
				.name( "br.com.tokiomarine.entity.mb.buddy.repositories." + theEntity.name() +"Repository")
				.annotateType( AnnotationDescription.Builder.ofType(Repository.class)
						.build())
				.annotateType( AnnotationDescription.Builder.ofType(Qualifier.class)
						.define("value",  theEntity.name()+"Repository")
						.build())
				.annotateType(		AnnotationDescription.Builder.ofType(RepositoryDefinition.class)
						.define("domainClass",clazz)
						.define("idClass", idClass)
						.build()
						)
				.make()
				.load(classLoader).getLoaded();
		System.out.println(cd.toGenericString());
		return cd;
	}

}
