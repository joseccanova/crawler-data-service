package org.nanotek.crawler.data.config.meta.classifier;

import schemacrawler.schema.PrimaryKey;

public class IdentityResult {

	public static final IdentityResult NULLRESULT = new IdentityResult(IdentityType.Null);

	private IdentityType type;
	
	private PrimaryKey key; 
	
	private Enum<?> subType;
	
	public IdentityResult() {
		super();
	}

	public IdentityResult(IdentityType type) {
		super();
		this.type = type;
	}
	
	public IdentityResult(IdentityType type, PrimaryKey key) {
		super();
		this.type = type;
		this.key = key;
	}

	public IdentityType getType() {
		return type;
	}

	public void setType(IdentityType type) {
		this.type = type;
	}

	public PrimaryKey getKey() {
		return key;
	}

	public void setKey(PrimaryKey key) {
		this.key = key;
	}
	
	
	public static enum IdentityType{
		Single,
		Composite,
		Null
	}
	
	public static enum SingleIdentityType {
		Identity , 
		Sequence , 
		Simple , 
		Null
	}
	
	public static enum CompositeIdentityType {
		IdClass , 
		Embembedable , 
		Null
	}

	public Enum<?> getSubType() {
		return subType;
	}

	public void setSubType(Enum<?> subType) {
		this.subType = subType;
	}
}

