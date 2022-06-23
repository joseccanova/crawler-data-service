package org.nanotek.crawler.data.config.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import schemacrawler.schema.Table;

@JsonInclude(value = Include.NON_NULL)
public class MetaClass implements IClass {

	@JsonProperty("tableName")
	protected String tableName;
	
	@JsonProperty("className")
	protected String className; 
	
	protected List<MetaDataAttribute> metaAttributes = new ArrayList<>();

	@JsonIgnore
	private boolean hasPrimeraryKey;

	@JsonIgnore
	private Table table;
	

	public MetaClass() {
		super();
	}

	public MetaClass(String tableName, String className, 
			List<MetaDataAttribute> metaAttributes) {
		super();
		this.tableName = tableName;
		this.className = className;
		this.metaAttributes = metaAttributes;
	}

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public void setClassName(String className) {
		this.className = className;
	}

	@Override
	public String getTableName() {
		return tableName;
	}

	@Override
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	@Override
	public List<MetaDataAttribute> getMetaAttributes() {
		return metaAttributes;
	}

	@Override
	public boolean  addMetaAttribute(MetaDataAttribute attr) {
		return metaAttributes.add(attr);
	}

	@Override
	public void hasPrimaryKey(boolean b) {
		this.hasPrimeraryKey = b;
	}

	@Override
	public boolean isHasPrimeraryKey() {
		return  metaAttributes !=null && metaAttributes.stream().filter(a -> a.isId()).count() > 0;
	}

	@Override
	public void setHasPrimeraryKey(boolean hasPrimeraryKey) {
		this.hasPrimeraryKey = hasPrimeraryKey;
	}

	public void setTable(Table t) {
		this.table = t;
	}

	public Table getTable() {
		return table;
	}
}
