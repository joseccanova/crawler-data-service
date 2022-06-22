package org.nanotek.crawler.data.util.db.support;

import org.nanotek.crawler.data.config.meta.MetaClass;

public class MensagemPostProcessor extends MetaClassPostProcessor<MetaClass> {

	@Override
	public void process(MetaClass metaClass) {
		if(metaClass.getTableName().contains("PTM0080_MENSG")) {
			metaClass
			.getMetaAttributes()
			.stream()
			.forEach (att -> {
				 if(att.getColumnName().equals("ID_MENSG")) { 
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
