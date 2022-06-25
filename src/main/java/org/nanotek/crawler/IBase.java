package org.nanotek.crawler;

import java.lang.reflect.Field;
import java.util.Arrays;

import javax.persistence.Id;

import org.nanotek.crawler.data.util.MutatorSupport;

@SuppressWarnings("unchecked")
public interface IBase<ID> extends BaseEntity<ID>{

	default ID getId() {
		return Arrays.asList(this.getClass()
				.getDeclaredFields())
				.stream()
				.filter(f -> f.getAnnotation(Id.class)!=null)
				.filter(f -> extracted(f)!=null)
				.map(f ->extracted(f)).findFirst().orElse(null);
	}

	default ID extracted(Field f) {
		return (ID) MutatorSupport.getProperty(f.getName(), this).orElse(null);
	};
	
}
