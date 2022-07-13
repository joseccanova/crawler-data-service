package org.nanotek.crawler.service;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.PathValidator;
import org.nanotek.crawler.data.config.meta.MetaEdge;
import org.springframework.beans.factory.annotation.Autowired;

public class SimplePathValidator implements PathValidator<Class<?>, MetaEdge> {
	
	@Autowired 
	GraphRelationsConfig<?,?> config;
	
	public SimplePathValidator() {
		super();
	}

	@Override
	public boolean isValidPath(GraphPath<Class<?>, MetaEdge> partialPath, MetaEdge edge) {
		return !edgeInAvoidList(edge) && !vertexInAvoidList(edge);
	}

	private boolean edgeInAvoidList(MetaEdge edge) {
		return config.getInvalidEdges().contains(edge);
	}
	
	private boolean vertexInAvoidList(MetaEdge edge) {
		return config.getInvalidVertex().contains(edge.getSource()) 
				|| config.getInvalidVertex().contains(edge.getTarget());
	}

}
