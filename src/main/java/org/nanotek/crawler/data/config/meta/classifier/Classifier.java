package org.nanotek.crawler.data.config.meta.classifier;

public interface Classifier<T , R> {

	R classify(T data);
	
}
