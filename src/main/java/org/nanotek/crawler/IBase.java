package org.nanotek.crawler;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface IBase<ID> {

	
	@JsonIgnore
	default ID getId() {
		return null;
	}
	
}
