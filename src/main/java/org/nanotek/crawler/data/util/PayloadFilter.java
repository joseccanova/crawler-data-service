package org.nanotek.crawler.data.util;

import java.util.Map;
import java.util.stream.Collectors;

import org.nanotek.crawler.data.stereotype.Filter;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PayloadFilter implements Filter<Map<String,Object>>{

	
	@Override
	public Map<String, Object> apply(Map<String, Object> payload) {
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
				|| isDotNotation(e.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
	
	private boolean isDotNotation(String key) {
		return key.contains(".") && key.split("[.]").length == 2;
	}
}