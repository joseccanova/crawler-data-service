package org.nanotek.crawler;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import javax.persistence.Id;
import javax.validation.Valid;

import org.apache.commons.beanutils.PropertyUtils;
import org.nanotek.crawler.data.config.meta.MetaClass;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@SuppressWarnings("serial")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
@Valid
public abstract class Base<K , ID> implements IBase<ID> , Serializable {

	private MetaClass metaClass;
	
	public static <K extends Base<? , ?>> K newType(Supplier<K> baseSupplier)
	{ 
		return baseSupplier.get();
	}
	
	@SuppressWarnings("unchecked")
	public ID getId()  {
		return (ID)Arrays.asList(this.getClass().getDeclaredFields())
		.stream()
		.filter(f -> f.getAnnotation(Id.class) !=null)
		.findFirst() 
		.map(f -> {
				try {
					return getProperty(f , this);
		} catch (Exception e) {
			e.printStackTrace();
			throw new NoSuchElementException();
		}})
		.orElseThrow();
	}

	private Object getProperty(Field f, Base<K, ID> base) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return 		PropertyUtils.getProperty(base, f.getName());
	}
	
	public MetaClass getMetaClass() {
		return this.metaClass;
	}
	
}
