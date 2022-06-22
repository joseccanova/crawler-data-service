package org.nanotek.crawler.data.config.meta;

import java.util.List;

public interface IClass {

	String getClassName();

	void setClassName(String className);

	String getTableName();

	void setTableName(String tableName);

	List<MetaDataAttribute> getMetaAttributes();

	boolean addMetaAttribute(MetaDataAttribute attr);

	void hasPrimaryKey(boolean b);

	boolean isHasPrimeraryKey();

	void setHasPrimeraryKey(boolean hasPrimeraryKey);

}