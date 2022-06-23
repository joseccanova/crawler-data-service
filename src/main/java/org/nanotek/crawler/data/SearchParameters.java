package org.nanotek.crawler.data;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchParameters {

	
	protected String inputClass1;
	
	protected String inputClass2;
	
	protected Map<String,Object> parameters;

}
