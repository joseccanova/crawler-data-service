package org.nanotek.crawler.legacy.stereotype;

import java.util.Optional;

public interface LongitudeAccessor<T> {

	default Optional<T> getLongitudePosition() {
		return Optional.empty();
	}
	
}
