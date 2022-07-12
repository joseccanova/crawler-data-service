package org.nanotek.crawler.data.domain.mongodb;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;

@Document
@EqualsAndHashCode
public class Identity implements MongoBase<String>{

	@Id
	protected String id;
	
	@JsonIgnore
	@Override
	public String id() {
		return id;
	}
	
	public Identity() {
		prepareId();
	}

	
	private void prepareId() {
		if (id == null || id.isEmpty())
			id =  new ObjectId().toHexString();		
	}

	@PersistenceConstructor 
	public Identity(String id) {
		super();
		this.id = id;
	}

}
