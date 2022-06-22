package org.nanotek.crawler.data.util.db.support;

import java.util.ArrayList;
import java.util.List;

import org.nanotek.crawler.data.config.meta.IClass;
import org.nanotek.crawler.data.config.meta.MetaClass;
import org.nanotek.crawler.data.util.db.JdbcHelper;

public class IdClassAttributeStrategyPostProcessor implements MetaClassPostPorcessor<IClass> {

	@Override
	public void verifyMetaClass(IClass instance) {
		String className = JdbcHelper.prepareName(instance.getClassName());
		instance
			.getMetaAttributes()
			.stream()
			.forEach (att -> {
				 if(att.getColumnName().equalsIgnoreCase("id")) { 
					 List<String> aliases = new ArrayList<>();
					 String classIdAlias = prepareClassName(className) + "Id";
					 aliases.add(classIdAlias);
					 String idClassAlias = "id"+className;
					 aliases.add(idClassAlias);
					 if (att.getIdAliases()==null) {
						 att.setIdAliases(aliases);
					 }else {
						 att.getIdAliases().addAll(aliases);
					 }
				 }
			});
	}

	private String prepareClassName(String className) {
		String firstLetter = className.substring(0, 1).toLowerCase();
		return firstLetter.concat(className.substring(1));
	}


}
