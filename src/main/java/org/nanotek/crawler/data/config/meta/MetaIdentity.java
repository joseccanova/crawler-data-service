package org.nanotek.crawler.data.config.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import schemacrawler.schema.PrimaryKey;
import schemacrawler.schema.TableConstraintColumn;

@Data
@Getter 
@Setter 
@EqualsAndHashCode
@ToString
public class MetaIdentity {

	@JsonIgnore
	@Exclude
	@lombok.ToString.Exclude
	protected PrimaryKey key;
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
			this.key = k;
			this.definition =this. key.getDefinition();
			this.shortName = this.key.getShortName();
			this.name = this.key.getName();
			prepareColumns(this.key.getColumns());
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


}
