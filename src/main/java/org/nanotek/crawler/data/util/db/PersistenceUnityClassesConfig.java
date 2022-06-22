package org.nanotek.crawler.data.util.db;


import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.nanotek.crawler.data.util.Cache;

import net.bytebuddy.TypeCache;
import net.bytebuddy.TypeCache.Sort;
import net.bytebuddy.dynamic.loading.InjectionClassLoader;

public class PersistenceUnityClassesConfig extends Cache<String , Class<?>>{

	private ConcurrentHashMap<String, Class<?>> entityStore;
	
	TypeCache<String> typeCache ;
	
	public PersistenceUnityClassesConfig() {
		super(Kind.STRONG , Kind.STRONG);
		typeCache = new TypeCache.WithInlineExpunction<>(Sort.SOFT); 
		constructMap();
	}

	private void constructMap() {
		entityStore = new ConcurrentHashMap<>(1000);
	}

	@Override
	public Class<?> create(String key) {
		return Optional.ofNullable(entityStore.get(key)).orElse(null);
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

	public TypeCache<String> getTypeCache() {
		return typeCache;
	}

}
