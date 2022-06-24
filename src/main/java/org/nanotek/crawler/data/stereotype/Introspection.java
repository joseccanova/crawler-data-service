package org.nanotek.crawler.data.stereotype;

import java.util.Optional;

public interface Introspection<T> {

	public static <T>Optional<Introspection<T>> empty(){
		return Optional.empty();
	}
	
}
