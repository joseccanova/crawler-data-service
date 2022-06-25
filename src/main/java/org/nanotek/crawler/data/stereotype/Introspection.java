package org.nanotek.crawler.data.stereotype;

import java.util.Optional;

public interface Introspection<T , I> {

	public static <T,I>Optional<Introspection<T,I>> empty(){
		return Optional.empty();
	}
	
	public abstract T instrospected();
	
}
