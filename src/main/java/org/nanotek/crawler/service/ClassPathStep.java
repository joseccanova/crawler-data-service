package org.nanotek.crawler.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.beanutils.WrapDynaBean;
import org.nanotek.beans.PropertyDescriptor;
import org.nanotek.crawler.data.domain.mongodb.ClassPath;
import org.nanotek.crawler.data.domain.mongodb.ClassPayloadDefinition;
import org.nanotek.crawler.data.domain.mongodb.PayloadAttribute;

import lombok.extern.slf4j.Slf4j;
import org.nanotek.crawler.data.util.*;

@Slf4j
public class ClassPathStep<T> extends ClassStep<T> {

	protected ClassPath classPath;
	
	public ClassPathStep() {
		super();
	}

	public ClassPathStep(Class<T> clazz , ClassPath classPath) {
		super(clazz);
		this.classPath = classPath;
	}

	public ClassPathStep(Class<T> clazz) {
		super(clazz);
	}
	@Override
	protected Map<String, Object> transform(T y) {
		try { 
			return  Optional
					.ofNullable(classPath)
					.map(cp -> generatePayload(cp.getPayloadDefinition() , y)).orElseThrow();
		}catch(Exception ex){
			return new HashMap<>();
		}
	}


	private Map<String,Object> generatePayload(ClassPayloadDefinition cp, T y) {
		Map<String,Object> theMap = new HashMap<>();
		cp.getAttributeFilters()
		.forEach(pa ->{
			try {
					populatePropertyPayload( theMap , pa,cp , y);
			}catch (Exception e) {
				log.info("error {}" , e);
			}
		});
		return theMap;
	}

	private void populatePropertyPayload( Map<String,Object> theMap , PayloadAttribute pa, ClassPayloadDefinition cp, T y) {
		WrapDynaBean wrap = new WrapDynaBean(y);
		if(checkAttribute(pa.getPayloadAttribute() , y) || checkAlias(pa.getAliases() , y)) {
				 Object value = wrap.get(pa.getPayloadAttribute());
				 theMap
				 		.put(y.getClass().getSimpleName().toLowerCase() 
				 				+ "." + pa.getPayloadAttribute(), value);
		}
	}
	

	private boolean checkAlias(List<String> alias, T y) {
		List<PropertyDescriptor> pds = MutatorSupport
		.getPropertyDescriptors(clazz)
		.map(pds1 -> Arrays.asList(pds1)).orElse(new ArrayList<>());
		return pds.stream()
				.anyMatch(pd -> checkAliases( alias, pd));
	}


	private boolean checkAliases(List<String> alias, PropertyDescriptor pd) {
		return Optional.ofNullable(alias).map(a -> a.stream().anyMatch(al -> pd.getName().equalsIgnoreCase(al))).orElse(false);
	}

	private boolean checkAttribute(String payloadAttribute ,  T y) {
		List<PropertyDescriptor> pds = MutatorSupport
		.getPropertyDescriptors(clazz)
		.map(pds1 -> Arrays.asList(pds1)).orElse(new ArrayList<>());
		return pds.stream()
					.anyMatch(pd -> pd.getName().equalsIgnoreCase(payloadAttribute));
	}

}
