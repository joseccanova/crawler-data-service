package org.nanotek.crawler.data.config.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import schemacrawler.schema.PrimaryKey;
import schemacrawler.schema.TableConstraintColumn;

@Data
@EqualsAndHashCode
@ToString
public class MetaIdentity {

	
	private String definition;
	private String shortName;
	private String name;
	private List<PkColumn> columns; 
	
	public MetaIdentity() {
		super();
	}

	public MetaIdentity(PrimaryKey key) {
		super();
		prepareMetaIdentity(key);
	}
	
	
	private void prepareMetaIdentity(PrimaryKey key2) {
		Optional.ofNullable(key2)
		.ifPresent(k-> {
			this.definition =key2.getDefinition();
			this.shortName = key2.getShortName();
			this.name = key2.getName();
			prepareColumns(key2.getConstrainedColumns());
		});
	}

	private void prepareColumns(List<TableConstraintColumn> columns2) {
		columns = new ArrayList<>();
		Optional.ofNullable(columns2)
		.ifPresent(cs -> {
			cs.stream()
			.forEach(c ->{
				PkColumn column = new PkColumn(c.getName());
				columns.add(column);
			});
		});
		
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<PkColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<PkColumn> columns) {
		this.columns = columns;
	}


}
