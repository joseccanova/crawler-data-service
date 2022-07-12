package org.nanotek.crawler.legacy.stereotype;

public interface Definition<T extends Definition<?>> {
	
	@SuppressWarnings("unchecked")
	default  <S extends T>   S get() {
		return (S) this;
	};

}
