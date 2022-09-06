package org.nanotek.crawler.data.config.meta;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.jgrapht.graph.DefaultEdge;
import org.nanotek.crawler.data.util.InstancePopulator;

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
@ToString
public class MetaEdge extends DefaultEdge  {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MetaEdge.class);

	public MetaEdge() {}
	
	public MetaEdge(Class<?> source , Class<?> target) {
		Class<?> c = MetaEdge.class.getSuperclass().getSuperclass();
		log.info("className{}" , c);
		for (Field f1 : c.getDeclaredFields()) {
			try {
					if (f1.getName().equals("source")){
						f1.setAccessible(true);
						f1.set(this, source);
					}
					if (f1.getName().equals("target")){
						f1.setAccessible(true);
						f1.set(this, target);
					}
				} catch (Exception e) {
				}
		}
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

