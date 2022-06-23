package org.nanotek.crawler.data.config.meta;


public class MetaRelation {

	protected MetaDataAttribute from; 
	
	protected MetaDataAttribute to;

	
	
	public MetaRelation(MetaDataAttribute from, MetaDataAttribute to) {
		super();
		this.from = from;
		this.to = to;
	}

	public MetaRelation() {
		super();
	}

	public MetaDataAttribute getFrom() {
		return from;
	}

	public void setFrom(MetaDataAttribute from) {
		this.from = from;
	}

	public MetaDataAttribute getTo() {
		return to;
	}

	public void setTo(MetaDataAttribute to) {
		this.to = to;
	}
	
}
