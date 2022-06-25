package org.nanotek.crawler.data.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.ConvertUtils;
import org.nanotek.beans.EntityBeanInfo;
import org.nanotek.beans.sun.introspect.PropertyInfo;
import org.nanotek.crawler.data.stereotype.Populator;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;


@SuppressWarnings({"unchecked","rawtypes"})
@Slf4j
public class  InstancePopulator<T> implements Populator<T, Map<String,Object>>{

	@Autowired
	SearchContextPayloadFilter  payloadFilter; 
	
	public InstancePopulator() {}
	
	@Override
	public T populate(T instance, Map<String, Object> payload) {
		payload.put("beanInstance", instance);
		Map<String,Object> parameters = Map.class.cast(payload.get("parameters"));
		payload.putAll(parameters);
		Map<String,Object> filteredPayload = payloadFilter.apply(payload);
		filteredPayload.entrySet()
		.stream()
		.forEach(e ->{
			try {
				String[] vvs = Optional.ofNullable(e).filter(e1 -> isNested(e1.getKey())).map(e1 -> e1.getKey().split("[.]")).orElse(new String[0]);//e.getKey().split("[.]");
				if (vvs.length==2) {
					
					boolean canBeClass =   isClass(vvs[0], instance);
					boolean canBeClassProperty = isClassProperty(vvs , instance) ;
					boolean isProperty = hasProperty(vvs , instance);
//					precedence on result to avoid side effects.. 
					String propertyStr = null;
					
					//in matter of fact each of these things will return an interesting thing
					if (canBeClass) {
						propertyStr=getClassProperty(vvs,instance);
					}else if (isClassProperty(vvs , instance)){
						propertyStr=vvs[0]+"id";
					}
					Boolean myResult =  isProperty || canBeClassProperty|| canBeClass ;

					if (myResult && propertyStr!=null) {
											try {
												PropertyInfo pd = getMethod(propertyStr , instance);
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
		Pattern pat = Pattern.compile(vvs[0] , Pattern.CASE_INSENSITIVE);
		if (pat.matcher(vvs[1]).find())
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
		return  vvs[1].toLowerCase();
	}

}