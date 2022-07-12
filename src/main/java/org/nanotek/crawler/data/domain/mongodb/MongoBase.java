package org.nanotek.crawler.data.domain.mongodb;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;


public interface MongoBase<T> extends Serializable{
	
	default String getId() {
		return id().toString();
	}
	
	T id();
}
