package org.nanotek.crawler.data.stereotype;

public interface Filter<T> {
	public T apply(T payload);
}