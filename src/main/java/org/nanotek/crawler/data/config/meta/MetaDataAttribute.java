package org.nanotek.crawler.data.config.meta;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.ToString;

@ToString
public class MetaDataAttribute implements IDataAttribute {

	@JsonProperty("isId")
	protected boolean isId = false;
	@JsonProperty("clazz")
	protected Class<?> clazz;
	@JsonProperty("columnName")
	protected String columnName;
	@JsonProperty("length")
	protected String length;
	@JsonProperty("required")
	protected boolean required;
	@JsonProperty("sqlType")
	private String sqlType;
	@JsonProperty("fieldName")
	private String fieldName;
	@JsonProperty("attributes")
	private Map<String, Object> attributes;
	
	private List<String> idAliases;
	
	@JsonIgnore
	private MetaClass metaClass;
	
	
	public MetaDataAttribute() {
		super();
	}

	public MetaDataAttribute(MetaClass mc) {
		super();
		this.metaClass=mc;
	}
	
	@Override
	public boolean isId() {
		return isId;
	}



	@Override
	public void setId(boolean isId) {
		this.isId = isId;
	}



	@Override
	public String getColumnName() {
		return columnName;
	}




	@Override
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}




	@Override
	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}


	@Override
	public Class<?> getClazz(){
		return this.clazz;
	}

	@Override
	public String getSqlType() {
		return sqlType;
	}

	@Override
	public void setSqlType(String string) {
		this.sqlType = string;
	}

	@Override
	public String getLength() {
		return length;
	}

	@Override
	public void setLength(String length) {
		this.length = length;
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public void setRequired(boolean required) {
		this.required = required;
	}

	@Override
	public void setFieldName(String name) {
		this.fieldName = name;
	}

	@Override
	public String getFieldName() {
		return fieldName;
	}

	@Override
	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public List<String> getIdAliases() {
		return idAliases;
	}

	@Override
	public void setIdAliases(List<String> idAliases) {
		this.idAliases = idAliases;
	}

	public MetaClass getMetaClass() {
		return metaClass;
	}

	public void setMetaClass(MetaClass metaClass) {
		this.metaClass = metaClass;
	}
	
	
}
