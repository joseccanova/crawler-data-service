package org.nanotek.crawler.data.util.bean;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.nanotek.crawler.data.domain.mongodb.ClassPath;

public class ClassPathInstancePopulator<T> extends InstancePopulator<T> {

	protected ClassPath clStep;
	
	public ClassPathInstancePopulator() {
	}
	
	
	public ClassPathInstancePopulator(ClassPath clStep) {
		super();
		this.clStep = clStep;
	}

	@Override
	public T populate(T instance, Map<String, Object> payload) {
		Map<String,Object> newPayload = new HashMap<>();
		clStep
		.getPayloadDefinition()
		.getAttributeFilters()
		.stream()
		.forEach(p -> {
			String name = p.getPayloadAttribute();
			Optional<Object> value = getValueIfPresent(payload , name);
				value.ifPresent(v -> newPayload.put(name, v));
		});
		
		return super.populate(instance, newPayload);
	}



	private Optional<Object> getValueIfPresent(Map<String, Object> payload, String name) {
		return payload.entrySet().stream()
		.filter(e -> e.getKey().equalsIgnoreCase(name))
		.map(e -> e.getValue())
		.findFirst();
	}


	public ClassPath getClStep() {
		return clStep;
	}



	public void setClStep(ClassPath clStep) {
		this.clStep = clStep;
	}
}
