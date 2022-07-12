package org.nanotek.crawler.service;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.PathValidator;
import org.nanotek.crawler.data.config.meta.MetaEdge;
import org.springframework.beans.factory.annotation.Autowired;

public class SimplePathValidator implements PathValidator<Class, MetaEdge> {
	
	@Autowired 
	GraphRelationsConfig<?,?> config;
	
	public SimplePathValidator() {
		super();
	}

	@Override
	public boolean isValidPath(GraphPath<Class, MetaEdge> partialPath, MetaEdge edge) {
		if (!edgeInAvoidList(edge))
			return true;
		return false;
	}

	private boolean edgeInAvoidList(MetaEdge edge) {
		return config.getInvalidEdges().contains(edge);
	}

}
