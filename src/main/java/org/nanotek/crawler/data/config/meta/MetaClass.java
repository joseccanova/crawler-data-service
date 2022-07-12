package org.nanotek.crawler.data.config.meta;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

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
	protected Table table;

	@JsonIgnore
	protected List<MetaRelationClass> metaRelationsClasses;

	protected MetaIdentity identity;

	private List<Table> referencedTables;
	
	public MetaClass() {
		super();
		this.referencedTables=new ArrayList<>();
		metaRelationsClasses = new ArrayList<> ();
	}

	public MetaClass(String tableName, String className, 
			List<MetaDataAttribute> metaAttributes) {
		super();
		this.tableName = tableName;
		this.className = className;
		this.metaAttributes = metaAttributes;
		this.referencedTables=new ArrayList<>();
	}

	
	
	public MetaClass(String tableName, String className, 
					Table table) {
		super();
		this.tableName = tableName;
		this.className = className;
		this.table = table;
		this.referencedTables=new ArrayList<>();
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

	public void addMetaRelationClass(MetaRelationClass mrc) {
		this.metaRelationsClasses.add(mrc);
		
	}

	public List<MetaRelationClass> getMetaRelationsClasses() {
		return metaRelationsClasses;
	}

	public MetaIdentity getIdentity() {
		return identity;
	}

	public void setIdentity(MetaIdentity identity) {
		this.identity = identity;
	}

	public void setMetaAttributes(List<MetaDataAttribute> metaAttributes) {
		this.metaAttributes = metaAttributes;
	}

	public void setMetaRelationsClasses(List<MetaRelationClass> metaRelationsClasses) {
		this.metaRelationsClasses = metaRelationsClasses;
	}

	public void addReferencedTable(Table referencedTable) {
		this.referencedTables.add(referencedTable);
	}

	public List<Table> getReferencedTables() {
		return referencedTables;
	}
}
