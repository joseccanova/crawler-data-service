package org.nanotek.crawler.data.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.beanutils.ConvertUtils;
import org.nanotek.beans.EntityBeanInfo;
import org.nanotek.beans.sun.introspect.PropertyInfo;
import org.nanotek.crawler.data.stereotype.Populator;

import lombok.extern.slf4j.Slf4j;


@SuppressWarnings({"unchecked","rawtypes"})
@Slf4j
public class  InstancePopulator<T> implements Populator<T, Map<String,Object>>{

	public InstancePopulator() {}
	
	@Override
	public T populate(T instance, Map<String, Object> payload) {
		Map<String,Object> parameters = Map.class.cast(payload.get("parameters"));
		payload.putAll(parameters);
		payload.entrySet()
		.stream()
		.forEach(e ->{
			try {
				String[] vvs = Optional.ofNullable(e).filter(e1 -> isNested(e1.getKey())).map(e1 -> e1.getKey().split("[.]")).orElse(new String[0]);//e.getKey().split("[.]");
				if (vvs.length==2) {
					
					boolean canBeClass =   isClass(vvs[0], instance);
					boolean canBeClassProperty = isClassProperty(vvs , instance) ;
					boolean isProperty = hasProperty(vvs , instance);
					
					Boolean myResult =  isProperty || canBeClassProperty|| canBeClass ;

					if (myResult) {
						String property =  isProperty ?   getProperty(vvs) : getClassProperty(vvs , instance);
						if ( isClassProperty(vvs , instance))
							property =  vvs[1].substring(instance.getClass().getSimpleName().length()); 
											try {
												PropertyInfo pd = getMethod(property , instance);
												if(pd !=null) {
													Object result = convert(e.getValue(),pd.getPropertyType());
													if(result !=null) {
														invoke(pd,instance,result);
													}
												}
											}catch(Exception ex){
												log.info("error {}" , ex);
											}
									}
				}
			} catch ( Exception e1) {
				log.info("error {}" , e1);
			}
		});
		return instance;
	}
	
	private void invoke(PropertyInfo pd, T instance, Object result) {
		try {
				pd.getWriteMethod().invoke(instance, result);
		}catch(Exception ex){
			log.info("error {}" , ex);
		}
	}

	private Object convert(Object value, Class<?> propertyType) {
		return ConvertUtils.convert(value, propertyType);
	}

	private boolean hasProperty(String[] vvs, T instance) {
		String property = getProperty(vvs);
		return Arrays.asList(instance.getClass().getDeclaredFields())
		.stream()
		.filter(f -> f.getName().equalsIgnoreCase(property))
		.count()>0;
	}
	
	private boolean isClass(String string, T instance) {
		return instance.getClass().getSimpleName().toLowerCase().equals(string);
	}

	private String getClassProperty(String[] vvs, T instance) {
		String property = vvs[1];
		EntityBeanInfo beanInfo = new EntityBeanInfo(instance.getClass());
		return beanInfo.getProperties()
		.entrySet()
		.stream()
		.filter(e -> e.getKey().equalsIgnoreCase(property))
		.map(e -> e.getKey()).findFirst().orElse(null);
	}

	private boolean isClassProperty(String[] vvs, T instance) {
		if (vvs[1].toLowerCase().contains(instance.getClass().getSimpleName().toLowerCase()+"id"))
			return true;
		return false;
	}

	private PropertyInfo getMethod(String key, T instance) {
		EntityBeanInfo beanInfo = new EntityBeanInfo(instance.getClass());
		return beanInfo.getProperties()
		.entrySet()
		.stream()
		.filter(e -> e.getKey().equalsIgnoreCase(key))
		.map(e -> e.getValue()).findFirst().orElse(null);
	}

	private boolean isNested(String key) {
		return key.contains(".");
	}
	
	private String getProperty(String[] vvs) {
		String idPart = vvs[1].substring(0,1).toUpperCase().concat(vvs[1].substring(1));
		String classPart = vvs[0].toLowerCase();
		return classPart + idPart;
	}

}