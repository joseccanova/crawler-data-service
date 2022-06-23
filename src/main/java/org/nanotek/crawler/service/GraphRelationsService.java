package org.nanotek.crawler.service;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.nanotek.crawler.Base;
import org.nanotek.crawler.data.config.meta.Id;
import org.nanotek.crawler.data.config.meta.MetaEdge;
import org.nanotek.crawler.data.util.db.JdbcHelper;
import org.nanotek.crawler.data.util.db.PersistenceUnityClassesConfig;
import org.nanotek.crawler.data.util.db.support.MetaClassPostPorcessor;
import org.nanotek.crawler.data.util.graph.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

//TODO: Remodel this class to construct a graph based on MetaClassVertex's and MetaEdge's


@Slf4j
@SpringBootConfiguration
@EnableScheduling
public class GraphRelationsService<T extends Base<?,?>> {

	@Autowired
	PersistenceUnityClassesConfig entityClassConfig;
	
	@Autowired
	RestClient restClient;
	
	List<MetaClassPostPorcessor<T>> instancePostProcessors = new ArrayList<>();

	
	public Map<?,?> getEntityClassConfig(){
		return entityClassConfig.keySet()
		.stream()
		.map(e -> Map.entry(e, entityClassConfig.get(e)) ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
	
	public GraphRelationsService() {
	}

	public Graph<Class<?> , MetaEdge>   mountRelationGraph() {
		Graph<Class<?> , MetaEdge> theGraph = prepareGraph() ;
		 processClassCache(theGraph);	
		 return theGraph;
	}



	private Graph<Class<?> , MetaEdge>  prepareGraph() {
		return GraphTypeBuilder.<Class<?>, MetaEdge> undirected(). allowingMultipleEdges(true)
				.allowingSelfLoops(true).edgeSupplier(MetaEdge::new).weighted(false).buildGraph();
	}


	private void processClassCache(Graph<Class<?> , MetaEdge>  theGraph) {
		processVertexRelations(theGraph );
	}


	private void processVertexRelations(Graph<Class<?> , MetaEdge>  theGraph) {
		log.info("begin processing relations ");
		entityClassConfig
		.keySet()
		.parallelStream()
		.forEach(e -> processRelations(theGraph, entityClassConfig.get(e)));
		
//		theGraph.vertexSet()
//		.parallelStream()
//		.forEach(v -> processRelations(theGraph , v));
		log.info("end processing relations ");
	}


	private Object processRelations(Graph<Class<?> , MetaEdge>  theGraph, Class<?> v) {
		if (!theGraph.containsVertex(v)) {
			log.info("vertex added {}" , v );
			theGraph.addVertex(v);
		}
		if (v.getAnnotation(javax.persistence.Id.class) ==null)
			return false;
		entityClassConfig
		.keySet()
		.parallelStream()
		.filter(v2 -> ! v.equals(entityClassConfig.get(v2)))
		.forEach(e -> verifyRelation (theGraph , v , entityClassConfig.get(e)));

		return true;
	}


	private  void verifyRelation(Graph<Class<?> , MetaEdge>  theGraph, Class<?> v, Class<?> v1) {
			processeRelationField( theGraph, v,v1);
	}


	private void processeRelationField(Graph<Class<?> , MetaEdge>  theGraph, Class<?> v, Class<?> v1) {
		if(!theGraph.containsVertex(v1)){
			log.info("vertex1 added {}" , v1 );
			theGraph.addVertex(v1);
		}
		AtomicInteger idx = new AtomicInteger();
		if (!v.equals(v1))
			Arrays.asList(v.getDeclaredFields()).parallelStream()
			.forEach(f -> {
				Arrays.asList(v1.getDeclaredFields()).parallelStream()
				.forEach(f1 -> {
					if(hasFieldEquivalence(f,f1)){
						synchronized(idx) {
							idx.addAndGet(1);
							if(!theGraph.containsEdge(v1 , v)) {
								theGraph.addEdge(v1, v);
									log.info("edge created {} {}",v , v1);
							}
						}
					}
				});
			});
	}


	private boolean hasFieldEquivalence(Field f, Field f1) {
		return Optional.ofNullable(f)
//		.filter(field -> isId(field) && !isId(f1))
		.filter(field -> fieldMapOver(field , f1)).isPresent();
	}

	private boolean fieldMapOver(Field field, Field f1) {
		PropertyEditor pe = PropertyEditorManager.findEditor(f1.getType());
		PropertyEditor peField = PropertyEditorManager.findEditor(field.getType());
		return pe !=null && peField !=null && hasFieldName(field,f1);
	}

	private boolean hasFieldName(Field field, Field f1) {
		String fieldName = field.getName() + "Id";
		fieldName = fieldName.substring(0, 1).toLowerCase().concat(fieldName.substring(1));
		return fieldName.equals(f1.getName());
	}



}

