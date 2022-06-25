package org.nanotek.crawler.data.util.db.support;

import java.util.ArrayList;
import java.util.List;

import org.nanotek.crawler.data.config.meta.MetaDataAttribute;
import org.nanotek.crawler.data.stereotype.Introspection;

import lombok.AllArgsConstructor;
import lombok.Getter;

@SuppressWarnings("unchecked")
@AllArgsConstructor
public class IdentityInstrospection<T extends List<MetaDataAttribute> , I extends Introspection<T , I>> implements Introspection<T,I>{

	@Getter
	protected T attributes;
	
	
	public IdentityInstrospection() {
		attributes = withDefaultList();
	}


	private T withDefaultList() {
		return (T) new ArrayList<MetaDataAttribute>();
	}

	@Override
	public T instrospected() {
		return  attributesAsIt(attributes);
	}

	private T attributesAsIt(T attributes2) {
		return attributes2;
	}
	
}
