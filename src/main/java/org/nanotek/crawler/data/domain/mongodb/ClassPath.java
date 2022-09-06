package org.nanotek.crawler.data.domain.mongodb;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.NoArgsConstructor;

@Document
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClassPath extends  Identity{
	
	private static final long serialVersionUID = 8749186657095081593L;

	String pathName;
	
	@Exclude
	ClassPayloadDefinition payloadDefinition;

	@PersistenceConstructor
	public ClassPath(String id , String pathName , ClassPayloadDefinition payloadDefinition) {
		super(id);
		this.pathName = pathName; 
		this.payloadDefinition = payloadDefinition;
	}

	public ClassPayloadDefinition getPayloadDefinition() {
		return payloadDefinition;
	}
	
}
