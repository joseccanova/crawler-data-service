package org.nanotek.crawler.data.util.db.support;

import org.nanotek.crawler.data.config.meta.MetaClass;
import org.nanotek.crawler.data.util.db.PostProcessor;

public abstract class MetaClassPostProcessor<T extends MetaClass> implements PostProcessor<T>{

	public MetaClassPostProcessor() {
		super();
	}
	
}
