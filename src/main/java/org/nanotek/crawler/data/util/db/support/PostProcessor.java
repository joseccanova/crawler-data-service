package org.nanotek.crawler.data.util.db.support;

public interface PostProcessor<T> {

	void process(T metaClass);
	
}
