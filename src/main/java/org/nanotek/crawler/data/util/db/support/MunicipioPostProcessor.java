package org.nanotek.crawler.data.util.db.support;

import org.nanotek.crawler.data.config.meta.MetaClass;

public class MunicipioPostProcessor extends MetaClassPostProcessor<MetaClass> {

	@Override
	public void process(MetaClass metaClass) {
		if(metaClass.getTableName().contains("MUNICIPIO")) {
			metaClass
			.getMetaAttributes()
			.stream()
			.forEach (att -> {
				 if(att.getColumnName().contains("CODMUNICIPIO")) { 
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
