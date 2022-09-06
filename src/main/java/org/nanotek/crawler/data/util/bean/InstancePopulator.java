package org.nanotek.crawler.data.util.bean;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.el.ExpressionFactory;

import org.apache.commons.beanutils.ConvertUtils;
import org.nanotek.beans.EntityBeanInfo;
import org.nanotek.beans.sun.introspect.PropertyInfo;
import org.nanotek.crawler.data.stereotype.Populator;
import org.nanotek.crawler.service.GraphRelationsConfig;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;


@SuppressWarnings({"unchecked","rawtypes"})
@Slf4j
public class  InstancePopulator<T> implements Populator<T, Map<String,Object>>{

	@Autowired
	SearchContextPayloadFilter  payloadFilter; 
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InstancePopulator.class);

	
	public InstancePopulator() {}
	
	@Override
	public T populate(T instance, Map<String, Object> payload) {
		payload.put("beanInstance", instance);
		Map<String,Object> parameters = Map.class.cast(payload.get("parameters"));
		if(parameters !=null)
			payload.putAll(parameters);
		
		payload
		.entrySet()
		.forEach(e ->{
			try {
				String[] vvs = Optional.ofNullable(e).filter(e1 -> isNested(e1.getKey())).map(e1 -> e1.getKey().split("[.]")).orElse(new String[0]);//e.getKey().split("[.]");
				if (vvs.length==2) {
					
					boolean canBeClass =   isClass(vvs[0], instance);
					boolean hasProperty =   hasProperty(vvs[1], instance);
					boolean canBeClassProperty = isClassProperty(vvs , instance) ;
					boolean isProperty = hasProperty(vvs , instance);
					boolean isClassIdProperty = isClassId(vvs , instance);
//					precedence on result to avoid side effects.. 
					String propertyStr = null;
					
					//in matter of fact each of these things will return an interesting thing
					if (canBeClass || hasProperty) {
						propertyStr=getClassProperty(vvs,instance);
					}else if (isClassProperty(vvs , instance)){
						propertyStr=vvs[0]+"id";
					}else if (isClassIdProperty)
					{ 
						propertyStr = vvs[1].substring(instance.getClass().getSimpleName().length());
					}
					Boolean myResult =  isProperty || canBeClassProperty|| canBeClass ;
					ExpressionFactory factory = ExpressionFactory.newInstance();

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
	
	private boolean hasProperty(String string, T instance) {
			if(getMethod(string, instance) !=null)
				return true;
			return false;
	}

	private boolean isClassId(String[] vvs, T instance) {
		if (vvs[1].length() < instance.getClass().getSimpleName().length())
			return false;
		String subs = vvs[1].substring(instance.getClass().getSimpleName().length());
		return getMethod(subs, instance)!=null;
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
		if(getMethod(vvs[0]+"id", instance) !=null)
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