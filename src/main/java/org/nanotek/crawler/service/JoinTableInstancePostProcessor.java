package org.nanotek.crawler.service;

import java.util.Map;

import org.nanotek.crawler.data.util.db.InstancePostProcessor;

public interface JoinTableInstancePostProcessor<T> extends InstancePostProcessor<T> {

		default Boolean isJoinTable(T instance) {
			return instance.getClass().getSimpleName().endsWith("_join");
		}
	
		@Override
		default T populateInstance(T instance, Map<String, Object> payload) {
			return InstancePostProcessor.super.populateInstance(instance, payload);
		}
}
