package org.nanotek.crawler.legacy.stereotype;

public interface Path<T>  {

	@SuppressWarnings("unchecked")
	default  <S extends T>   S get() {
		return (S) this;
	};

}
