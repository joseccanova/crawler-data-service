package org.nanotek.crawler.data.util.db;

import java.beans.PropertyEditorManager;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public interface PayloadFilter {

	default Map<String,Object> filterPayload(Map<String,Object> payload){
		return payload.entrySet()
		.stream()
		.filter(e ->{
			return hasCriteria(e);
		})
		.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	default boolean hasCriteria(Entry<String, Object> e) {
		boolean hasEditor = PropertyEditorManager.findEditor(e.getValue().getClass()) !=null;
		Pattern pattern = Pattern.compile("^id|id$|^cd|^nm|^nr|^login|^numid|^cod" , Pattern.CASE_INSENSITIVE);
		boolean isId = pattern.matcher(e.getKey()).find();
		return hasEditor && isId;
	};
	
}
