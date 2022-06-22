package org.nanotek.crawler.data.util.db;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Wrapper for {@link ObjectProvider}.
 *
 * @param <T> type of the object to fetch
 * @author Spencer Gibb
 */
public class SimpleObjectProvider<T> implements ObjectProvider<T> {

	private final T object;

	public SimpleObjectProvider(T object) {
		this.object = object;
	}

	@Override
	public T getObject(Object... args) throws BeansException {
		return this.object;
	}

	@Override
	public T getIfAvailable() throws BeansException {
		return this.object;
	}

	@Override
	public T getIfUnique() throws BeansException {
		return this.object;
	}

	@Override
	public T getObject() throws BeansException {
		return this.object;
	}

}