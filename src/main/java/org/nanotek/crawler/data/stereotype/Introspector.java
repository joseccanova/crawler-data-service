package org.nanotek.crawler.data.stereotype;

import java.util.Optional;

import org.nanotek.crawler.data.util.MutatorSupport;

public interface Introspector<T> extends MutatorSupport<T> {

	default Optional<Introspection<T>> instrospect(T t){
		return Introspection.empty();
	}
	
}
