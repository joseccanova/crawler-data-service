package org.nanotek.crawler.data.util.db.support;

import org.nanotek.crawler.data.config.meta.MetaClass;

public class OficinaPostProcessor2 extends MetaClassPostProcessor<MetaClass> {

	@Override
	public void process(MetaClass metaClass) {
		if(metaClass.getTableName().contains("OFICINA")) {
			metaClass
			.getMetaAttributes()
			.stream()
			.forEach (att -> {
				 if(att.getColumnName().equals("CODOFI")) { 
					 att.setId(true);
					 att.setRequired(true);
				 }
				 else { 
					 att.setId(false);
				 }
			});
		}
	}

}
