package org.nanotek.crawler.data.config.meta;

import java.util.List;
import java.util.Map;

public interface IDataAttribute {

	java.lang.String toString();

	boolean isId();

	void setId(boolean isId);

	String getColumnName();

	void setColumnName(String columnName);

	void setClazz(Class<?> clazz);

	Class<?> getClazz();

	String getSqlType();

	void setSqlType(String string);

	String getLength();

	void setLength(String length);

	boolean isRequired();

	void setRequired(boolean required);

	void setFieldName(String name);

	String getFieldName();

	void setAttributes(Map<String, Object> attributes);

	Map<String, Object> getAttributes();

	List<String> getIdAliases();

	void setIdAliases(List<String> idAliases);

}