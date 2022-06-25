package org.nanotek.crawler.data.stereotype;

import java.util.Optional;

import org.nanotek.crawler.data.util.MutatorSupport;

public interface Introspector<T,I> extends MutatorSupport<T> {

	default <S extends Introspector<T,S>> Optional<Introspection<T, S>> instrospect(){
		return Introspection.empty();
	}
	
	default boolean isValid(Class<?> clazz) {
		return true;
	}
	
}
