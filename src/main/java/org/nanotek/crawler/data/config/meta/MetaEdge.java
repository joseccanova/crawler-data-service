package org.nanotek.crawler.data.config.meta;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.jgrapht.graph.DefaultEdge;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

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
@Builder
@Slf4j
@ToString
public class MetaEdge extends DefaultEdge  {


	public MetaEdge() {}
	
	public MetaEdge(Class<?> source , Class<?> target) {
		Arrays.asList(MetaEdge.class.getClasses())
		.stream()
		.forEach(c -> {
			try {
					if (c.getField("source") !=null){
						Field f = c.getField("source");
						f.setAccessible(true);
						f.set(this, source);
					}
					if (c.getField("target") !=null){
						Field f = c.getField("target");
						f.setAccessible(true);
						f.set(this, target);
						
					}
				} catch (Exception e) {
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
	
	@Override
	public boolean equals(Object obj) {
			if (obj == null)
				return false;
			MetaEdge eob = MetaEdge.class.cast(obj);
			if (eob.getSource()==null && this.getSource() !=null)
				return false;
			if(eob.getTarget()==null && this.getTarget()!=null)
				return false;
			return eob.getSource()!=null 
			&& 
					eob.getSource().equals(this.getSource())
			&& 
			eob.getTarget() !=null 
			&& 
			eob.getTarget().equals(this.getTarget());
	}

}

