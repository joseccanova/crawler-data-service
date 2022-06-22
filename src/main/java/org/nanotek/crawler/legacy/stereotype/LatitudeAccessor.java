package org.nanotek.crawler.legacy.stereotype;

import java.util.Optional;

public interface LatitudeAccessor<T> {

	default Optional<T> getLatitudePosition() {
		return Optional.empty();
	}
}
