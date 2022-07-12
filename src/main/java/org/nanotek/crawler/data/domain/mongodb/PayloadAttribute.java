package org.nanotek.crawler.data.domain.mongodb;

import java.util.List;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PayloadAttribute extends  Identity{

	protected String payloadAttribute;
	
	protected String payloadClassString;
	
	protected List<String> aliases;

	@PersistenceConstructor 
	public PayloadAttribute(String id , String payloadAttribute, String payloadClassString, List<String> aliases) {
		super(id);
		this.payloadAttribute = payloadAttribute;
		this.payloadClassString = payloadClassString;
		this.aliases = aliases;
	}
	
	
}
