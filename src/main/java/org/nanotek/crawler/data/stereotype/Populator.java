package org.nanotek.crawler.data.stereotype;

public 	interface Populator <T,P>{
	
	T populate(T instance , P payload);
	
}