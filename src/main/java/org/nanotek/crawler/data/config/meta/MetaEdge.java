package org.nanotek.crawler.data.config.meta;

import java.util.Arrays;

import org.jgrapht.graph.DefaultEdge;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

/**
 * What is about, left and right is a common vocabulary on graphs representation. 
 * the attribute left represent what precedes. to right happens left shall happens first.
 * 
 * It's also intended to not steal CODD definitions on referential integrity since is not a 
 * referential integrity. It's an abstraction that defines that left shall happens first than the right
 * in a context that is intended to find the "rights" based on an assumption on "lefts". 
 * 
 * @author T807630
 *
 */
@SuppressWarnings("serial")
@Data
@Builder
public class MetaEdge extends DefaultEdge  {


	public MetaEdge() {}
	
	public MetaEdge(Class<?> source , Class<?> target) {
		Arrays.asList(DefaultEdge.class.getDeclaredFields())
		.forEach(f ->{
			f.setAccessible(true);
			try {
				if (f.getName().equals("source"))
				f.set(this, source);
				else if (f.getName().equals("target"))
					f.set(this, target);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	@Override
	@JsonProperty(value = "target")
	public Object getTarget() {
		return super.getTarget();
	}
	@Override
	@JsonProperty(value ="source")
	public Object getSource() {
		return super.getSource();
	}

}

