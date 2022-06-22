package org.nanotek.crawler.data.util.db.support;

import org.nanotek.crawler.data.config.meta.IClass;

public interface PostProcessor<T> {

	void process(IClass cm11);
	
}
