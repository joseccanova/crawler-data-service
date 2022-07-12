package org.nanotek.crawler.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.WrapDynaBean;

import lombok.extern.slf4j.Slf4j;

/**
 * Shall notice that is not an obligation that clazz be equals to <T>.class
 * @author T807630
 *
 * @param <T>
 */
@Slf4j
public class ClassStep<T> {

	protected Class<T> clazz;
	
	protected Function<T , Map<String,Object>> payloadFields = (y) -> transform(y);

	protected Map<String,Object> transform(T y) {
		String className = y.getClass().getSimpleName().toLowerCase();
		Map<String,Object> payload =new HashMap<>();
		Pattern pat = Pattern.compile("id$|^id|^num|^cod|^cd|prima$" , Pattern.CASE_INSENSITIVE);
		WrapDynaBean wrap = new WrapDynaBean(y);
		Arrays.asList(clazz.getDeclaredFields())
		.stream()
		.filter(f -> pat.matcher(f.getName()).find())
		.forEach(f -> {
			try {
				Object value = wrap.get(f.getName());
				payload.put(className + "." + f.getName().toLowerCase(), value);
			} catch (Exception e) {
				log.debug("error {}" , e);
			}
		});
		return payload;
	}

	public ClassStep() {
		super();
	}

	public ClassStep(Class<T> clazz) {
		super();
		this.clazz = clazz;
	}
	
	
	public Map<String,Object> getInstancePayload(T t) {
		return payloadFields.apply(t);
	}
	
	
}
