package org.nanotek.crawler.data.util.db;

public interface PostProcessor<T> {

	void process(T metaClass);
	
}
