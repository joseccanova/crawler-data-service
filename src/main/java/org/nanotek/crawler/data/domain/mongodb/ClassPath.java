package org.nanotek.crawler.data.domain.mongodb;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.NoArgsConstructor;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClassPath extends  Identity{
	
	String pathName;
	
	@Exclude
	ClassPayloadDefinition payloadDefinition;

	@PersistenceConstructor
	public ClassPath(String id , String pathName , ClassPayloadDefinition payloadDefinition) {
		super(id);
		this.pathName = pathName; 
		this.payloadDefinition = payloadDefinition;
	}
	
}
