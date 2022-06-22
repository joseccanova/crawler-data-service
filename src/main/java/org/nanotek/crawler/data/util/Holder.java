package org.nanotek.crawler.data.util;

import java.util.Optional;

public class Holder<S> {
	S s;
	public Holder<S> put(S s){
		this.s = s;
		return  this;
	}
	public  Optional<S> get(){
		return Optional.ofNullable(s);
	}
	
	@Override
	public String toString() {
		return "Holder [s=" + s + "]";
	}
	
	
}