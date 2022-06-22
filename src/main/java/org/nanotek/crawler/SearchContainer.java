package org.nanotek.crawler;

import java.util.Map;

public class SearchContainer <T extends BaseEntity<?>>{

	protected T entity;
	
	protected Map<String,Object> sortParameters;

	public SearchContainer() {
		super();
	}

	public SearchContainer(T entity, Map<String, Object> parameters) {
		super();
		this.entity = entity;
		this.sortParameters = parameters;
	}

	public T getEntity() {
		return entity;
	}

	public void setEntity(T entity) {
		this.entity = entity;
	}

	public Map<String, Object> getSortParameters() {
		return sortParameters;
	}

	public void setSortParameters(Map<String, Object> sortParameters) {
		this.sortParameters = sortParameters;
	}

}
