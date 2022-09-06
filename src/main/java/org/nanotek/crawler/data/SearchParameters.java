package org.nanotek.crawler.data;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchParameters {

	
	protected String inputClass1;
	
	protected String inputClass2;
	
	protected Map<String,Object> parameters;

	public String getInputClass1() {
		return inputClass1;
	}

	public void setInputClass1(String inputClass1) {
		this.inputClass1 = inputClass1;
	}

	public String getInputClass2() {
		return inputClass2;
	}

	public void setInputClass2(String inputClass2) {
		this.inputClass2 = inputClass2;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

}
