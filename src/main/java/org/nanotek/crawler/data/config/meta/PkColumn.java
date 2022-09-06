package org.nanotek.crawler.data.config.meta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode
public class PkColumn {

	protected String name;

	public PkColumn() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PkColumn(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
