package org.nanotek.crawler.data.util;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.nanotek.crawler.data.stereotype.Filter;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SearchContextPayloadFilter implements Filter<Map<String,Object>>{

	
	@Override
	public Map<String, Object> apply(Map<String, Object> payload) {
		Object instance = payload.get("beanInstance");
		return payload.entrySet().stream()
				.filter(e -> e.getKey()!=null && !e.getKey().isEmpty() && e.getValue() !=null)
				.filter(e -> e.getKey().equals("inputClass1") 	|| 
				e.getKey().equals("inputClass2") ||
				e.getKey().equals("visited")
				|| e.getKey().equals("graph")
				|| e.getKey().equals("path")
				|| e.getKey().split("instance[0-9]+").length>1
				|| e.getKey().equals("node")
				|| e.getKey().equals("nextStep")
				|| e.getKey().equals("parameters")
				|| isDotNotation(e.getKey()) && isIdRelated(e.getKey()) && !importedId(instance, e.getKey()) )
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
	
	private boolean importedId(Object instance, String key) {
		return Arrays
		.asList(instance.getClass().getDeclaredFields())
		.stream()
		.filter(f -> Pattern.matches(f.getName().toLowerCase(), key))
		.count()>0;
	}

	private boolean isIdRelated(String key) {
		Pattern pat = Pattern.compile("^id|id$" , Pattern.CASE_INSENSITIVE);
		return pat.matcher(key).find();
	}

	private boolean isDotNotation(String key) {
		return key.contains(".") && key.split("[.]").length == 2;
	}
}