package org.nanotek.crawler.data.util.db;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;

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

public class RepositoryClassesConfig   {

	private ConcurrentHashMap<String, Class<?>> entityStore;

	public RepositoryClassesConfig(){
		constructMap() ;
	}

	private void constructMap() {
		entityStore = new ConcurrentHashMap<>(1000);
	}


	public Class<?> prepareReppositoryForClass(Class<?> clazz , Class<?> idClass, ClassLoader classLoader){
		Generic typeDescription = TypeDescription.Generic.Builder.parameterizedType( EntityBaseRepository.class, clazz , idClass).build().asGenericType();
		Entity theEntity = clazz.getAnnotation(Entity.class);
		Optional.ofNullable(theEntity).orElseThrow();
		Class<?> cd =   new ByteBuddy(ClassFileVersion.JAVA_V8)
//				.makeInterface(EntityBaseRepository.class)
				.makeInterface(typeDescription)
				.name( "org.nanotek.data.entity.mb.buddy.repositories." + theEntity.name() +"Repository")
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

	public int size() {
		return entityStore.size();
	}

	public boolean isEmpty() {
		return entityStore.isEmpty();
	}

	public Class<?> get(Object key) {
		return entityStore.get(key);
	}

	public boolean containsKey(Object key) {
		return entityStore.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return entityStore.containsValue(value);
	}

	public Class<?> put(String key, Class<?> value) {
		return entityStore.put(key, value);
	}

	public void putAll(Map<? extends String, ? extends Class<?>> m) {
		entityStore.putAll(m);
	}

	public Class<?> remove(Object key) {
		return entityStore.remove(key);
	}

	public void clear() {
		entityStore.clear();
	}

	public KeySetView<String, Class<?>> keySet() {
		return entityStore.keySet();
	}

	public Collection<Class<?>> values() {
		return entityStore.values();
	}

	public Set<Entry<String, Class<?>>> entrySet() {
		return entityStore.entrySet();
	}

	public int hashCode() {
		return entityStore.hashCode();
	}

	public String toString() {
		return entityStore.toString();
	}

	public boolean equals(Object o) {
		return entityStore.equals(o);
	}

	public Class<?> putIfAbsent(String key, Class<?> value) {
		return entityStore.putIfAbsent(key, value);
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

	public Class<?> computeIfAbsent(String key, Function<? super String, ? extends Class<?>> mappingFunction) {
		return entityStore.computeIfAbsent(key, mappingFunction);
	}

	public Class<?> computeIfPresent(String key,
			BiFunction<? super String, ? super Class<?>, ? extends Class<?>> remappingFunction) {
		return entityStore.computeIfPresent(key, remappingFunction);
	}

	public Class<?> compute(String key,
			BiFunction<? super String, ? super Class<?>, ? extends Class<?>> remappingFunction) {
		return entityStore.compute(key, remappingFunction);
	}

	public Class<?> merge(String key, Class<?> value,
			BiFunction<? super Class<?>, ? super Class<?>, ? extends Class<?>> remappingFunction) {
		return entityStore.merge(key, value, remappingFunction);
	}

	public boolean contains(Object value) {
		return entityStore.contains(value);
	}

	public Enumeration<String> keys() {
		return entityStore.keys();
	}

	public Enumeration<Class<?>> elements() {
		return entityStore.elements();
	}

	public long mappingCount() {
		return entityStore.mappingCount();
	}

	public KeySetView<String, Class<?>> keySet(Class<?> mappedValue) {
		return entityStore.keySet(mappedValue);
	}

	public void forEach(long parallelismThreshold, BiConsumer<? super String, ? super Class<?>> action) {
		entityStore.forEach(parallelismThreshold, action);
	}

	public <U> void forEach(long parallelismThreshold,
			BiFunction<? super String, ? super Class<?>, ? extends U> transformer, Consumer<? super U> action) {
		entityStore.forEach(parallelismThreshold, transformer, action);
	}

	public <U> U search(long parallelismThreshold,
			BiFunction<? super String, ? super Class<?>, ? extends U> searchFunction) {
		return entityStore.search(parallelismThreshold, searchFunction);
	}

	public <U> U reduce(long parallelismThreshold,
			BiFunction<? super String, ? super Class<?>, ? extends U> transformer,
			BiFunction<? super U, ? super U, ? extends U> reducer) {
		return entityStore.reduce(parallelismThreshold, transformer, reducer);
	}

	public double reduceToDouble(long parallelismThreshold,
			ToDoubleBiFunction<? super String, ? super Class<?>> transformer, double basis,
			DoubleBinaryOperator reducer) {
		return entityStore.reduceToDouble(parallelismThreshold, transformer, basis, reducer);
	}

	public long reduceToLong(long parallelismThreshold, ToLongBiFunction<? super String, ? super Class<?>> transformer,
			long basis, LongBinaryOperator reducer) {
		return entityStore.reduceToLong(parallelismThreshold, transformer, basis, reducer);
	}

	public int reduceToInt(long parallelismThreshold, ToIntBiFunction<? super String, ? super Class<?>> transformer,
			int basis, IntBinaryOperator reducer) {
		return entityStore.reduceToInt(parallelismThreshold, transformer, basis, reducer);
	}

	public void forEachKey(long parallelismThreshold, Consumer<? super String> action) {
		entityStore.forEachKey(parallelismThreshold, action);
	}

	public <U> void forEachKey(long parallelismThreshold, Function<? super String, ? extends U> transformer,
			Consumer<? super U> action) {
		entityStore.forEachKey(parallelismThreshold, transformer, action);
	}

	public <U> U searchKeys(long parallelismThreshold, Function<? super String, ? extends U> searchFunction) {
		return entityStore.searchKeys(parallelismThreshold, searchFunction);
	}

	public String reduceKeys(long parallelismThreshold,
			BiFunction<? super String, ? super String, ? extends String> reducer) {
		return entityStore.reduceKeys(parallelismThreshold, reducer);
	}

	public <U> U reduceKeys(long parallelismThreshold, Function<? super String, ? extends U> transformer,
			BiFunction<? super U, ? super U, ? extends U> reducer) {
		return entityStore.reduceKeys(parallelismThreshold, transformer, reducer);
	}

	public double reduceKeysToDouble(long parallelismThreshold, ToDoubleFunction<? super String> transformer,
			double basis, DoubleBinaryOperator reducer) {
		return entityStore.reduceKeysToDouble(parallelismThreshold, transformer, basis, reducer);
	}

	public long reduceKeysToLong(long parallelismThreshold, ToLongFunction<? super String> transformer, long basis,
			LongBinaryOperator reducer) {
		return entityStore.reduceKeysToLong(parallelismThreshold, transformer, basis, reducer);
	}

	public int reduceKeysToInt(long parallelismThreshold, ToIntFunction<? super String> transformer, int basis,
			IntBinaryOperator reducer) {
		return entityStore.reduceKeysToInt(parallelismThreshold, transformer, basis, reducer);
	}

	public void forEachValue(long parallelismThreshold, Consumer<? super Class<?>> action) {
		entityStore.forEachValue(parallelismThreshold, action);
	}

	public <U> void forEachValue(long parallelismThreshold, Function<? super Class<?>, ? extends U> transformer,
			Consumer<? super U> action) {
		entityStore.forEachValue(parallelismThreshold, transformer, action);
	}

	public <U> U searchValues(long parallelismThreshold, Function<? super Class<?>, ? extends U> searchFunction) {
		return entityStore.searchValues(parallelismThreshold, searchFunction);
	}

	public Class<?> reduceValues(long parallelismThreshold,
			BiFunction<? super Class<?>, ? super Class<?>, ? extends Class<?>> reducer) {
		return entityStore.reduceValues(parallelismThreshold, reducer);
	}

	public <U> U reduceValues(long parallelismThreshold, Function<? super Class<?>, ? extends U> transformer,
			BiFunction<? super U, ? super U, ? extends U> reducer) {
		return entityStore.reduceValues(parallelismThreshold, transformer, reducer);
	}

	public double reduceValuesToDouble(long parallelismThreshold, ToDoubleFunction<? super Class<?>> transformer,
			double basis, DoubleBinaryOperator reducer) {
		return entityStore.reduceValuesToDouble(parallelismThreshold, transformer, basis, reducer);
	}

	public long reduceValuesToLong(long parallelismThreshold, ToLongFunction<? super Class<?>> transformer, long basis,
			LongBinaryOperator reducer) {
		return entityStore.reduceValuesToLong(parallelismThreshold, transformer, basis, reducer);
	}

	public int reduceValuesToInt(long parallelismThreshold, ToIntFunction<? super Class<?>> transformer, int basis,
			IntBinaryOperator reducer) {
		return entityStore.reduceValuesToInt(parallelismThreshold, transformer, basis, reducer);
	}

	public void forEachEntry(long parallelismThreshold, Consumer<? super Entry<String, Class<?>>> action) {
		entityStore.forEachEntry(parallelismThreshold, action);
	}

	public <U> void forEachEntry(long parallelismThreshold, Function<Entry<String, Class<?>>, ? extends U> transformer,
			Consumer<? super U> action) {
		entityStore.forEachEntry(parallelismThreshold, transformer, action);
	}

	public <U> U searchEntries(long parallelismThreshold,
			Function<Entry<String, Class<?>>, ? extends U> searchFunction) {
		return entityStore.searchEntries(parallelismThreshold, searchFunction);
	}

	public Entry<String, Class<?>> reduceEntries(long parallelismThreshold,
			BiFunction<Entry<String, Class<?>>, Entry<String, Class<?>>, ? extends Entry<String, Class<?>>> reducer) {
		return entityStore.reduceEntries(parallelismThreshold, reducer);
	}

	public <U> U reduceEntries(long parallelismThreshold, Function<Entry<String, Class<?>>, ? extends U> transformer,
			BiFunction<? super U, ? super U, ? extends U> reducer) {
		return entityStore.reduceEntries(parallelismThreshold, transformer, reducer);
	}

	public double reduceEntriesToDouble(long parallelismThreshold,
			ToDoubleFunction<Entry<String, Class<?>>> transformer, double basis, DoubleBinaryOperator reducer) {
		return entityStore.reduceEntriesToDouble(parallelismThreshold, transformer, basis, reducer);
	}

	public long reduceEntriesToLong(long parallelismThreshold, ToLongFunction<Entry<String, Class<?>>> transformer,
			long basis, LongBinaryOperator reducer) {
		return entityStore.reduceEntriesToLong(parallelismThreshold, transformer, basis, reducer);
	}

	public int reduceEntriesToInt(long parallelismThreshold, ToIntFunction<Entry<String, Class<?>>> transformer,
			int basis, IntBinaryOperator reducer) {
		return entityStore.reduceEntriesToInt(parallelismThreshold, transformer, basis, reducer);
	}

}
