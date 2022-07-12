package org.nanotek.crawler.data.util.functions;

import java.util.function.Function;

public interface WrapTransformer <T,S> extends Function<T,S>{

	@Override
	default S apply(T t) {
		return null;
	}

}
