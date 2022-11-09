package org.nanotek.crawler.data.config.meta.classifier;

import schemacrawler.schema.PrimaryKey;

public class IdentityResult {

	public IdentityResult(IdentityType type) {
		super();
		this.type = type;
	}

	public IdentityResult() {
		super();
	}

	private IdentityType type;
	
	private PrimaryKey key ; 
	
	public IdentityType getType() {
		return type;
	}

	public void setType(IdentityType type) {
		this.type = type;
	}

	public static enum IdentityType{
		Single,
		Composite,
		Null
	}

	public IdentityResult(IdentityType type, PrimaryKey key) {
		super();
		this.type = type;
		this.key = key;
	}

	public PrimaryKey getKey() {
		return key;
	}

	public void setKey(PrimaryKey key) {
		this.key = key;
	}
	
}

