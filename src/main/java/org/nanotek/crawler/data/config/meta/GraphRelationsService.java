package org.nanotek.crawler.data.config.meta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.WrapDynaBean;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.BellmanFordShortestPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.nanotek.crawler.Base;
import org.nanotek.crawler.SearchContainer;
import org.nanotek.crawler.data.util.db.support.IdClassAttributeStrategyPostProcessor;
import org.nanotek.crawler.data.util.db.support.MetaClassPostPorcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

//TODO: Remodel this class to construct a graph based on MetaClassVertex's and MetaEdge's


@Slf4j
@SpringBootConfiguration
@EnableScheduling
public class GraphRelationsService<T extends Base<?,?>> {

	Graph<Class<?>, ?> relationGraph; 

	@Autowired
	@Qualifier("classCache")
	@Lazy(true)
	Map<Class<?> , String> classCache;
	
	
	List<MetaClassPostPorcessor<T>> instancePostProcessors = new ArrayList<>();

	public GraphRelationsService() {
	}

	public Graph<Class<?>, ?>  mountRelationGraph() {
		Graph<Class<?> , ?> theGraph = GraphTypeBuilder.<Class<?>, DefaultEdge> undirected().allowingMultipleEdges(false)
				.allowingSelfLoops(false).edgeClass(MetaEdge.class).weighted(false).buildGraph();
		if(relationGraph == null) {
			relationGraph = theGraph;	
			processClassCache( classCache, relationGraph);		
		}
		return relationGraph;
	}



	private void processClassCache(Map<Class<?>, String> classCache2, Graph<Class<?> , ?>  theGraph) {
		classCache2.entrySet()
		.stream()
		.forEach(e -> processEntry( e , theGraph));
		processVertexRelations(theGraph);
	}


	private void processVertexRelations(Graph<Class<?> , ?>  theGraph) {
		log.info("processing relations ");
		theGraph.vertexSet()
		.stream()
		.forEach(v -> processRelations(theGraph , v));
		log.info("end processing relations ");
	}


	private Object processRelations(Graph<Class<?>, ?> theGraph, Class<?> v) {
		theGraph
		.vertexSet()
		.stream()
		.forEach(v1 -> verifyRelation (theGraph , v , v1));

		return true;
	}


	private  void verifyRelation(Graph<Class<?>, ?> theGraph, Class<?> v, Class<?> v1) {
		if (containsField(v,v1)) {
			processeRelationField( theGraph, v,v1);
		}
	}


	private void processeRelationField(Graph<Class<?>, ?> theGraph, Class<?> v, Class<?> v1) {
		if (!v.equals(v1))
			Arrays.asList(v.getDeclaredFields()).stream()
			.forEach(f -> {
				Arrays.asList(v1.getDeclaredFields()).stream()
				.forEach(f1 -> {
					if(f1.getName().equals(f.getName())){
						if(!theGraph.containsEdge(v , v1)) {
							if (f.getAnnotation(Id.class)!=null) {
								theGraph.addEdge(v , v1);
							}
						}
					}
				});
			});
	}


	private boolean containsField(Class<?> v, Class<?> v1) {
		if (v.equals(v1))
			return false;
		return Arrays.asList(v.getDeclaredFields()).stream()
				.filter(f -> {
					return verifyField (v1 , f);
				}).count() > 0;


	}

	private boolean verifyField(Class<?> v1, Field f) {
		Pattern pattern = Pattern.compile("^id|^cd|^nm|^nr|^login|^numid" , Pattern.CASE_INSENSITIVE);
		if (pattern.matcher(f.getName()).find())
			return Arrays.asList(v1.getDeclaredFields()).stream()
					.filter(f1 -> f1.getName().toLowerCase().contains(f.getName().toLowerCase())|| f.getName().toLowerCase().equals(f1.getName().toLowerCase()))
					.count() > 0;
					return false;
	}


	private boolean processEntry(Entry<Class<?>, String> e, Graph<Class<?>, ?> theGraph) {
		boolean result = false;
		if (!theGraph.containsVertex(e.getKey())) {
			result = theGraph.addVertex(e.getKey());
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	void processShortesPath(Graph<Class<?>,MetaEdge> theGraph , Class<?> v , Class<?> v1) {
		try {
			BellmanFordShortestPath bf = new BellmanFordShortestPath(theGraph);
			GraphPath g1 = bf.getPath(v, v1);
		}catch(Exception ex) {log.debug("error {}" , ex);}
	}



}

