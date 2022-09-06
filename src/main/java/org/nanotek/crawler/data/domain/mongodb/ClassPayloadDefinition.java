package org.nanotek.crawler.data.domain.mongodb;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.PersistenceConstructor;
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
public class ClassPayloadDefinition extends  Identity{

	String classAlias; 
	
	String inputClass1;
	
	String inputClass2;
	
	@Exclude
	List<PayloadAttribute> attributeFilters;
	
	@Exclude 
	List<PayloadAttribute> attributeExtractions;
	
	@PersistenceConstructor 
	public ClassPayloadDefinition(String id , String classAlias , String inputClass1, String inputClass2,
			 List<PayloadAttribute> attributeFilters, List<PayloadAttribute> attributeExtractions) {
		super(id);
		this.classAlias = classAlias;
		this.inputClass1 = inputClass1;
		this.inputClass2 = inputClass2;
		this.attributeFilters = attributeFilters;
		this.attributeExtractions = attributeExtractions;
	}

	public List<PayloadAttribute>  getAttributeFilters() {
		return attributeFilters;
	}
}
