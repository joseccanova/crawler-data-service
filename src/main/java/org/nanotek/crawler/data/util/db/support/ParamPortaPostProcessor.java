package org.nanotek.crawler.data.util.db.support;

import org.nanotek.crawler.data.config.meta.MetaClass;

public class ParamPortaPostProcessor extends MetaClassPostProcessor<MetaClass> {

	@Override
	public void process(MetaClass metaClass) {
		if(metaClass.getTableName().contains("PTM0032_PARAM_PRTAL")) {
			metaClass
			.getMetaAttributes()
			.stream()
			.forEach (att -> {
				 if(att.getColumnName().equals("ID_PARAM_PRTAL")) { 
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
