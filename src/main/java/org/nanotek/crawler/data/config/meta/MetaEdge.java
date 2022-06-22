package org.nanotek.crawler.data.config.meta;

import java.util.Optional;

import org.jgrapht.graph.DefaultEdge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaEdge extends DefaultEdge implements IMetaEdge{

	protected Class<?> left; 
	
	protected Class<?> right;
	
	protected RelationType type; 
	
	
	@Override
	public Object getSource() {
		return left(super.getSource());
	}


	private Object left(Object object) {
		return left = Class.class.cast(object);
	}
	
	@Override
	public Class<?> getLeft(Object object){
		return Class.class.cast(object);
	}
	
	@Override
	public Object getTarget() {
		return right(super.getTarget());
	}


	private Object right(Object object) {
		return right = getRight(object);
	}
	
	@Override
	public Class<?> getRight(Object object) {
		return Class.class.cast(object);
	}


	@Override
	public RelationType getType()
	{ 
		return Optional.ofNullable(type).orElse(RelationType.ONE);
	}
	
}

