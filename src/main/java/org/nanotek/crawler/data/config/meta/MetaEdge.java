package org.nanotek.crawler.data.config.meta;

import lombok.AllArgsConstructor;
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
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetaEdge {

	protected MetaClassVertex left; 
	
	protected MetaClassVertex right;
	
	protected RelationType type; 
	
	
}

