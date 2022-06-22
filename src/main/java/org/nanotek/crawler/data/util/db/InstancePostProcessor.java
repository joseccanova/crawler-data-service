package org.nanotek.crawler.data.util.db;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.WrapDynaBean;

public interface InstancePostProcessor<T> {

	default T populateInstance(T instance , Map<String,Object> payload) {
		Map<String,Object> filtered = new PayloadFilter(){}.filterPayload(payload);
		WrapDynaBean wp = whithDynaBean (instance);
		Arrays.asList(instance.getClass().getDeclaredFields())
		.stream()
		.forEach(f ->{
			Optional.ofNullable(filtered.get(f.getName()))
			.ifPresent(v ->{
				Object value = ConvertUtils.convert(v, f.getType());
				findEditorFor(f)
				.ifPresent(ped -> {
					ped.setValue(value);
					wp.set(f.getName(), ped.getValue());
				});
			});
		});
		return instance;
	}
	
	default WrapDynaBean whithDynaBean(T instance) {
		return new WrapDynaBean(instance);
	}
	
	default Optional<PropertyEditor> findEditorFor(Field f){
		return Optional.ofNullable(PropertyEditorManager.findEditor(f.getType()));
	}
	
	
}
