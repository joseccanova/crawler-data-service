package org.nanotek.crawler.service;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.WrapDynaBean;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.BidirectionalDijkstraShortestPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.nanotek.crawler.Base;
import org.nanotek.crawler.data.SearchParameters;
import org.nanotek.crawler.data.config.meta.MetaClass;
import org.nanotek.crawler.data.config.meta.MetaEdge;
import org.nanotek.crawler.data.config.meta.TempClass;
import org.nanotek.crawler.data.domain.mongodb.ClassPath;
import org.nanotek.crawler.data.mongo.repositories.ClassPathRepository;
import org.nanotek.crawler.data.stereotype.EntityBaseRepository;
import org.nanotek.crawler.data.stereotype.InstancePostProcessor;
import org.nanotek.crawler.data.util.ClassPathInstancePopulator;
import org.nanotek.crawler.data.util.InstancePopulator;
import org.nanotek.crawler.data.util.MutatorSupport;
import org.nanotek.crawler.data.util.db.PayloadFilter;
import org.nanotek.crawler.data.util.db.PersistenceUnityClassesConfig;
import org.nanotek.crawler.data.util.db.RepositoryClassesConfig;
import org.nanotek.crawler.data.util.graph.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import schemacrawler.schema.Table;

//TODO: Remodel this class to construct a graph based on MetaClassVertex's and MetaEdge's

@SuppressWarnings({ "rawtypes", "unchecked" })
@Slf4j
@SpringBootConfiguration
@EnableScheduling
public class GraphRelationsConfig<T extends Base<?,?> , R extends EntityBaseRepository<T, ?>> 
implements MutatorSupport<T>{

	@Autowired
	PersistenceUnityClassesConfig entityClassConfig;
	
	@Autowired
	RestClient restClient;
	
	List<InstancePostProcessor<T>> instancePostProcessors = new ArrayList<>();
	
	List<MetaEdge> invalidEdges = new ArrayList<>();
	
	public  List<MetaEdge> getInvalidEdges() {
		return invalidEdges;
	}
	
	private Graph<Class<?>, MetaEdge> entityGraph;

	@Autowired
	DefaultListableBeanFactory beanFactory;
	
	public Map<?,?> getEntityClassConfig(){
		return entityClassConfig.keySet()
		.parallelStream()
		.map(e -> Map.entry(e, entityClassConfig.get(e)) ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
	
	public GraphRelationsConfig() {
		super();
	}

	public Graph<Class<?> , MetaEdge>   mountRelationGraph() {
		try { 
		Graph<Class<?> , MetaEdge> theGraph = prepareGraph() ;
		if(entityGraph == null)
		{
		 processClassCache(theGraph);	
		 this.entityGraph = theGraph;
		}
		 return (this.entityGraph );
		}catch(Exception ex) {
			log.info("error {} ", ex);
			this.entityGraph=null;
			throw new RuntimeException(ex);
		}
	}



	private Graph<Class<?> , MetaEdge>  prepareGraph() {
		return GraphTypeBuilder.<Class<?>, MetaEdge> undirected(). allowingMultipleEdges(true)
				.allowingSelfLoops(true).edgeSupplier(MetaEdge::new).weighted(false).buildGraph();
	}

	@Autowired
	ObjectMapper mapper;
	
	public GraphPath<Class<?> , MetaEdge> getGraphPath(JsonNode jsonNode) {
		try {
		TempClass map= mapper.convertValue(jsonNode, TempClass.class);
		Class<?> sourcls = beanFactory.getBeanClassLoader().loadClass(map.getSource());
		Class<?> targetcls = beanFactory.getBeanClassLoader().loadClass(map.getTarget());
		Graph<Class<?> , MetaEdge> relationGraph = mountRelationGraph();
		BidirectionalDijkstraShortestPath<Class<?>, MetaEdge> djkstra = new BidirectionalDijkstraShortestPath<>(relationGraph);
		return djkstra.getPath(sourcls, targetcls);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	
	private void processClassCache(Graph<Class<?> , MetaEdge>  theGraph) {
		List processing = processCreateVertex(theGraph);
		if (!processing.isEmpty())
			processVertexRelations(theGraph );
	}


	private List<Boolean> processCreateVertex(Graph<Class<?> , MetaEdge>  theGraph) {
		return entityClassConfig
		.keySet()
		.parallelStream()
		.map(k -> {
				Class<?> c = entityClassConfig.get(k);
				if (!theGraph.containsVertex(c)) {
					log.info("vertex added {}" , c );
					theGraph.addVertex(c);
				}
				return true;
		}).collect(Collectors.toList());
	}

	private void processVertexRelations(Graph<Class<?> , MetaEdge>  theGraph) {
		List<MetaEdge> edgesTests = new ArrayList<>();
		log.info("begin processing relations ");
		entityClassConfig
		.keySet()
		.parallelStream()
		.forEach(e -> processRelations(theGraph, entityClassConfig.get(e) , edgesTests));
		log.info("end processing relations ");
	}


	private Object processRelations(Graph<Class<?> , MetaEdge>  theGraph, Class<?> v, List<MetaEdge> edgesTests) {
		if (!theGraph.containsVertex(v)) {
			log.info("vertex added {}" , v );
			theGraph.addVertex(v);
		}
		entityClassConfig
		.keySet()
		.parallelStream()
		.forEach(e -> verifyRelation (theGraph , v , entityClassConfig.get(e) , edgesTests));
		return true;
	}


	private  void verifyRelation(Graph<Class<?> , MetaEdge>  theGraph, Class<?> v, Class<?> v1, List<MetaEdge> edgeTests) {
			Class<T> cls1 = (Class<T>)v;
			Class<T> cls2 = (Class<T>)v1;
			verifyForeignKeys(theGraph , Optional.ofNullable(cls1) , Optional.ofNullable(cls2) );
			
//			if (notInGraph(v,v1,edgeTests))
//					processeRelationField( theGraph, v,v1 , edgeTests);
	}
	
	AtomicInteger ai = new AtomicInteger();
	
	private void processeRelationField1(Graph<Class<?> , MetaEdge>  theGraph, Class<?> v, Class<?> v1, List<MetaEdge> edgesTests) {
		
		if (!v.equals(v1)) {
			Arrays.asList(v.getDeclaredFields()).stream()
			.filter(f -> f.getAnnotation(javax.persistence.Id.class) !=null)
			.forEach(f -> {
				Arrays.asList(v1.getDeclaredFields()).stream()
				.forEach(f1 -> {
					if(hasFieldEquivalence(f,f1)){
						synchronized(ai){
							if(!theGraph.containsVertex(v1)){
								log.info("vertex1 added {}" , v1 );
								theGraph.addVertex(v1);
							}
							MetaEdge edgeTest = new MetaEdge(v , v1);
							
								if(!theGraph.containsEdge(edgeTest)) {
									theGraph.addEdge(v, v1 ,edgeTest);
									log.info("the edge {}->{} countedges {}" , v , v1 , ai.getAndAdd(1));
									edgesTests.add(edgeTest);
								}
						}
						}
				});
			});
		}
	}


	private Boolean notInGraph(Class<?> v, Class<?> v1, List<MetaEdge> edgesTests) {
			MetaEdge edgeTest = new MetaEdge(v,v1); 
			return !edgesTests.contains(edgeTest) ;
	}

	private boolean hasFieldEquivalence(Field f, Field f1) {
		return Optional.ofNullable(f)
//		.filter(field -> isId(field) && !isId(f1))
		.map(field -> fieldMapOver(field , f1)).filter(result -> result==true).isPresent();
	}

	private boolean fieldMapOver(Field field, Field f1) {
		return hasFieldName(field,f1) && hasPropertyEditor(field,f1) ;
	}

	private boolean hasPropertyEditor(Field field, Field f1) {
		PropertyEditor pe = PropertyEditorManager.findEditor(f1.getType());
		PropertyEditor peField = PropertyEditorManager.findEditor(field.getType());
		return pe !=null && peField !=null;	
	}

	private boolean hasFieldName(Field field, Field f1) {
		Pattern pat = Pattern.compile("^cod|^id|^num|^cd|id$");
		Boolean simpleEquivalence = false;
		if (pat.matcher(field.getName()).find())
			simpleEquivalence = f1.getName().equalsIgnoreCase(field.getName());
		return simpleEquivalence; 
	}

	public Graph<Class<?>, MetaEdge> getEntityGraph() {
		return entityGraph;
	}

	public void setEntityGraph(Graph<Class<?>, MetaEdge> entityGraph) {
		this.entityGraph = entityGraph;
	}


	@Bean(value="inputChannel")
	MessageChannel inputChannel(){
		return MessageChannels.direct("inputChannel").get();
	}
	
	@Bean(value="inputChannelTemplate")
	@Qualifier(value="inputChannelTemplate")
	MessagingTemplate inputTemplate() {
		return new MessagingTemplate(inputChannel());
	}

	@Bean(value="outputChannel")
	MessageChannel outputChannel(){
		return MessageChannels.direct("outputChannel").get();
	}
	
	public static final String CLASS1 = "inputClass1";
	public static final String CLASS2 = "inputClass2";
	public static final String GRAPH = "graph";
	public static final String PARAMETERS = "parameters";
	public static final String PATH = "path"; 
	public static final String SIMPLE = "simple"; 
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object prepareGraphPayload(Object m) {
		SearchParameters parameters = SearchParameters.class.cast(m);
		Map<String,Object> payload = new HashMap<>();
		List<Class<?>> visited = new ArrayList<>();
		payload.put(VISITED, visited);
		String inputClass1 = parameters.getInputClass1();
		payload.put(CLASS1, inputClass1);
		Optional<Class<T>> clazz1 = createClass(inputClass1);
		String inputClass2 = parameters.getInputClass2();
		payload.put(CLASS2, inputClass2);
		Optional<Class<T>> clazz2 = createClass(inputClass2);
		payload.putAll(parameters.getParameters());
		payload.put(PARAMETERS, parameters.getParameters());
		DijkstraShortestPath<Class<?> , MetaEdge> bf = new DijkstraShortestPath<>(checkGraph(payload , clazz1 , clazz2));
		GraphPath<Class<?> , MetaEdge> g1 = bf.getPath(clazz1.get(),clazz2.get());
		payload.put(PATH, g1);
		return payload;	
	}
	

	private Graph<Class<?>, MetaEdge>  checkGraph( Map payload, Optional<Class<T>> clazz1,
			Optional<Class<T>> clazz2) {
		Graph<Class<?> , MetaEdge> theGraph = null;
		if (payload.get(SIMPLE) !=null) {
			theGraph = GraphTypeBuilder.<Class<?>, MetaEdge> undirected().allowingMultipleEdges(false)
					.allowingSelfLoops(true).edgeClass(MetaEdge.class).weighted(false).buildGraph();
			theGraph.addVertex(clazz1.get());
			if(!theGraph.containsVertex(clazz2.get())){
				theGraph.addVertex(clazz2.get());
				theGraph.addEdge(clazz1.get(), clazz2.get());
			}else {
				theGraph.addEdge(clazz1.get(), clazz1.get());
			}
			log.info("simple graph {}" , payload.get(SIMPLE));
		}else {
			theGraph = mountRelationGraph();
		}
		payload.put(GRAPH, theGraph);
		return theGraph;
	}
	
	private void verifyForeignKeys(Graph<Class<?>, MetaEdge> entityGraph2, Optional<Class<T>> clazz1,
			Optional<Class<T>> clazz2) {
		
		clazz2.filter(c2 -> !c2.equals(clazz1.get())).ifPresent(c ->{
				T instance = createIntance(c);
				MetaClass meta = instance.getMetaClass();
				meta.getMetaRelationsClasses()
				.stream()
				.forEach(mr ->{
					Table tt = mr.getForeignKeyTable();
					T instance2 = createIntance(clazz1.get());
					if (instance2.getMetaClass().getTableName().equals(tt.getFullName())) {
						if(!entityGraph2.containsVertex(clazz2.get())){
							entityGraph2.addVertex(clazz2.get());
							entityGraph2.addEdge(clazz1.get(), clazz2.get());
						}else {
							entityGraph2.addEdge(clazz1.get(), clazz2.get());
						}
						log.info("added relation clazz1 {} {}" , clazz1,clazz2);
					}
				});
		});
	}

	private Optional<Class<T>> createClass(String classStr){
		try {
			return  Optional.ofNullable( (Class<T>) beanFactory.getBeanClassLoader().loadClass(classStr));
		} catch (Exception e) {
			return Optional.empty();
		}
	}
	
	@Bean
	IntegrationFlow createInputGraphIntegrationFlow(){
		return IntegrationFlows.from(inputChannel())
				.transform(m -> {
					return prepareGraphPayload(m);
				})
				.channel(outputChannel())
				.get();
	}
	
	@Bean
	IntegrationFlow createOutputIntegrationFlow(){
		return IntegrationFlows.from(outputChannel())
				.transform(m -> {
					Map payload = Map.class.cast(m);
					return processOutputChannelPayload(payload);
				})
				.route(m -> checkEndpath(m))
				.get();
	}
	
	@Bean
	IntegrationFlow nillChannelFlow(){
		return IntegrationFlows.from(nillChannel())
				.transform(m -> m)
				.get();
	}
	
	private Object processOutputChannelPayload(Object m) {
		Map payload = Map.class.cast(m);
		String inputClass1 = String.class.cast(payload.get(CLASS1));
		GraphPath<Class<?> , MetaEdge> g1 = GraphPath.class.cast(payload.get(PATH));
		Class<?> clazz1 = createClass(inputClass1).get();
		String inputClass2 = String.class.cast(payload.get(CLASS2));
		Class<?> clazz2 = createClass(inputClass2).get();
		Object msg1 =  processaClassePayload(m, inputClass1);
		Map orElse = Map.class.cast(msg1);
		getNextPath(m , g1 , Optional.of(clazz1)).ifPresentOrElse(c -> {
			addVisited(clazz1 , msg1); 
			processNextStep(Map.class.cast(msg1) , g1 , c);
		}, () -> { orElse.put(CLASS1, Class.class.cast(clazz2).getName() ); log.info("what is happening? {} " , orElse);});
		return msg1;		
	}
	
	public static final String NEXTSTEP = "nextStep";
	private void processNextStep(Map payload, GraphPath<Class<?>, MetaEdge> g1, Object c) {
		payload.put(CLASS1, Class.class.cast(c).getName());
		payload.put(NEXTSTEP, c);
	}
	
	
	private Class addVisited(Class<?> c , Object m) {
		Map payload = Map.class.cast(m);
		List<Class<?>> visited = List.class.cast(payload.get(VISITED));
		if (!visited.contains(c))
			visited.add(c);
		return c;
	}
	
	@Bean("nillChannel")
	MessageChannel nillChannel() {
		return MessageChannels.direct("nillChannel").get();
	}

	
	private MessageChannel checkEndpath(Object m) {
		Map payload = Map.class.cast(m);
		List<Class<?>> visited = List.class.cast(payload.get(VISITED));
		Class<?> nextStep = (Class<?>) payload.get(NEXTSTEP);

		return Optional.ofNullable(nextStep)
				.map(n -> {
					GraphPath<Class<?>, MetaEdge> path = GraphPath.class.cast(payload.get(PATH));
					return  visited.contains(nextStep) ?  nillChannel() : outputChannel();
				}).orElse(nillChannel());
	}

	
	@Autowired
	@Lazy(true)
	RepositoryClassesConfig repositoryClassesConfig;
	
	public List<?> getRepositories(){
		return repositoryClassesConfig.keySet().parallelStream().collect(Collectors.toList());
	}
	
	public R getRepository(T instance) {
		Object value = repositoryClassesConfig.get(instance.getClass().getSimpleName());
		if (value ==null) throw new RuntimeException("value is null");
		Class<R> rcls = Class.class.cast(value);
		return 	beanFactory.getBean(rcls);

	}
	
	private   Object processaClassePayload(Object m , String classStr) {
		try {
			Class<T> clazzCls= Class.class.cast(beanFactory.getBeanClassLoader().loadClass(classStr));
			Map<String,Object> payload = Map.class.cast(m);
			T instance = populateInstance(createIntance(clazzCls) , payload);
			validateInstance(instance);
			List<?> result = prepareRepository(instance, payload);
			Map<String,Object> newPayload = filterPayload(payload);
			return processNode(objectMapper.convertValue(result, JsonNode.class) , newPayload , Class.class.cast(instance.getClass()));
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void validateInstance(T instance) {
		if (Arrays.asList(instance.getClass().getDeclaredFields()).stream().filter(f -> filterFieldName(f.getName())).noneMatch(f -> checkPropertyInstance(f , instance))){
			throw new RuntimeException("none value present on processing payload for query");
		}
	}

	public boolean filterFieldName(String fName) {
		return !fName.contains("$");
	}
	
	public boolean checkPropertyInstance (Field f , T instance) {
		WrapDynaBean bean = new WrapDynaBean(instance);
		return bean.get(f.getName()) !=null;
	}
	
	@Autowired
	ObjectMapper objectMapper;
	
	private  Map<String, Object> processNode(JsonNode returnNode, Map<String, Object> payload , Class<T> clzz) {
		return Optional.ofNullable(returnNode)
				.map(rn -> {
					if (rn.isArray() && rn.size()>=1) 
						for (int i = 0 ; i < rn.size() ; i++) {
							payload.put("counter" , i);
							processNode(rn.get(i) , payload , clzz);
						}
					else {
						if (!rn.isArray()) {
							payload.putIfAbsent("node", rn);
							T any = objectMapper.convertValue  (rn, clzz);
							Optional.ofNullable(any)
							.ifPresent(any1->				
							{ 
								String sufix = "instance";
								String sufixResult = Optional.ofNullable(payload.get("counter"))
										.map(c ->{
											return sufix+c+"_";
										}).orElse(sufix+"_");

								payload.put(sufixResult + any1.getClass().getSimpleName() , any1);
								payload.put(any1.getClass().getSimpleName() , any1);
								populateInstanceToPayload(any1 , payload);
							});
						}
					}
					return payload;
				}).orElse(payload);
	}
	
	@Autowired
	protected PayloadFilter  payloadFilter;
	
	private Map<String, Object> filterPayload(Map<String, Object> payload) {
		return payloadFilter.apply(payload);
	}

	protected <P> Optional<?> populateInstanceToPayload(P pojo, Map payload) {
		ClassStep<P> cp = ClassStep.class.cast(getClassStep(payload , pojo.getClass()));
		Map<String,Object> payloadpojo = cp.getInstancePayload(pojo);
		payload.putAll(payloadpojo);
		return Optional.ofNullable(payload);
	}

	@Autowired
	ClassPathRepository classPathRepository;
	private static final String CP_ID = "classPathId";
	private <P>  ClassStep<P> getClassStep(Map payload , Class<P> clazz) {
		
		String cpId =  String.class.cast(payload.get(CP_ID));
		
		if (cpId == null || cpId.isEmpty()) {
			return new ClassStep(clazz);
		}
		
		return classPathRepository.findById(cpId)
		.map(cp -> ClassStep.class.cast( new  ClassPathStep(clazz , cp)))
		.orElse(new ClassStep(clazz));
	}

	public List<T> prepareRepository(T instance , Map<String,Object> payload){
		ExampleMatcher matcher = ExampleMatcher.matchingAll().withStringMatcher(StringMatcher.EXACT).withIgnoreNullValues();
		Example<T> example = Example.of(instance,matcher);
		return getRepository(instance).findAll(example , PageRequest.of(0, 1000)).toList();
	}
	
	@SuppressWarnings("deprecation")
	private   T createIntance(Class<T> crtorCls) {
		try {
			return crtorCls.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	@Autowired 
	@Lazy(true)
	InstancePopulator<T> instancePopulator;
	
	private  T populateInstance(T instance, Map<String, Object> payload) {
		if (payload.get(CP_ID) !=null)
		{
				Optional<ClassPath> cp =  Optional.ofNullable(payload.get(CP_ID))
				.map(cn -> classPathRepository.findById(String.class.cast(cn)).orElseThrow());
		return  (T) cp.map(cp1 -> new ClassPathInstancePopulator<>(cp1).populate(instance , payload) ).orElse(instancePopulator.populate(instance, payload));
		}
		return  instancePopulator.populate(instance, payload);
	}
	public static final String VISITED = "visited";
	
	private Optional<?> getNextPath(Object m , GraphPath<Class<?>, MetaEdge> g1, Optional<Class<?>> clazzz) {
		return clazzz.map(c -> {
			Map payload = Map.class.cast(m);
			List<Class<?>> visited = List.class.cast(payload.get(VISITED));
			return g1.getEdgeList().stream()
					.filter(e -> !visited.contains(e.getSource()))
					.filter(e -> !visited.contains(e.getTarget()))
					.map(e -> {
						return processClass(e , c);
					}).findAny().orElse(c);
		});
	}
	
	private Class processClass(MetaEdge e, Class<?> c ) {
		return c.equals(e.getSource())? Class.class.cast(e.getTarget()):Class.class.cast(e.getSource());
	}
}

