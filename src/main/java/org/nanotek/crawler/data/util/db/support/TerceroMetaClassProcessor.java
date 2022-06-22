package org.nanotek.crawler.data.util.db.support;

import org.nanotek.crawler.data.config.meta.MetaClass;

public class TerceroMetaClassProcessor extends MetaClassPostProcessor<MetaClass> {

	@Override
	public void process(MetaClass metaClass) {
		if(metaClass.getTableName().contains("TERCERO")) {
			metaClass
			.getMetaAttributes()
			.stream()
			.forEach(att -> {
				if (att.getColumnName().contains("NUMID"))
					{ 
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
