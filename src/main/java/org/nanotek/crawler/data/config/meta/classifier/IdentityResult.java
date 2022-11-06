package org.nanotek.crawler.data.config.meta.classifier;

public class IdentityResult {

	public IdentityResult(IdentityType type) {
		super();
		this.type = type;
	}

	public IdentityResult() {
		super();
	}

	private IdentityType type;
	
	public IdentityType getType() {
		return type;
	}

	public void setType(IdentityType type) {
		this.type = type;
	}

	public static enum IdentityType{
		Single,
		Composite
	}
	
}

