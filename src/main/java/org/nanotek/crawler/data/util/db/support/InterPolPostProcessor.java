package org.nanotek.crawler.data.util.db.support;

import org.nanotek.crawler.data.config.meta.MetaClass;

public class InterPolPostProcessor extends MetaClassPostProcessor<MetaClass> {

	@Override
	public void process(MetaClass metaClass) {
		if(metaClass.getTableName().contains("PART_INTER_POL")) {
			metaClass
			.getMetaAttributes()
			.stream()
			.forEach (att -> {
				 if(att.getColumnName().contains("IDEPOL")) { 
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
