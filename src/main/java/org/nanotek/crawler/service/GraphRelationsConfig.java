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
import org.nanotek.crawler.BaseEntity;
import org.nanotek.crawler.data.SearchParameters;
import org.nanotek.crawler.data.config.meta.MetaClass;
import org.nanotek.crawler.data.config.meta.MetaEdge;
import org.nanotek.crawler.data.config.meta.TempClass;
import org.nanotek.crawler.data.stereotype.EntityBaseRepository;
import org.nanotek.crawler.data.stereotype.InstancePostProcessor;
import org.nanotek.crawler.data.util.InstancePopulator;
import org.nanotek.crawler.data.util.MutatorSupport;
import org.nanotek.crawler.data.util.PayloadFilter;
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
	

	private Graph<Class<?>, MetaEdge> entityGraph;

	@Autowired
	DefaultListableBeanFactory beanFactory;
	
	public Map<?,?> getEntityClassConfig(){
		return entityClassConfig.keySet()
		.stream()
		.map(e -> Map.entry(e, entityClassConfig.get(e)) ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
	
	public GraphRelationsConfig() {
	}

	public Graph<Class<?> , MetaEdge>   mountRelationGraph() {
		Graph<Class<?> , MetaEdge> theGraph = prepareGraph() ;
		if(entityGraph == null)
		{
		 processClassCache(theGraph);	
		 this.entityGraph = theGraph;
		}
		 return (this.entityGraph );
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
		processVertexRelations(theGraph );
	}


	private void processVertexRelations(Graph<Class<?> , MetaEdge>  theGraph) {
		log.info("begin processing relations ");
		entityClassConfig
		.keySet()
		.parallelStream()
		.forEach(e -> processRelations(theGraph, entityClassConfig.get(e)));
		log.info("end processing relations ");
	}


	private Object processRelations(Graph<Class<?> , MetaEdge>  theGraph, Class<?> v) {
		if (!theGraph.containsVertex(v)) {
			log.info("vertex added {}" , v );
			theGraph.addVertex(v);
		}
		entityClassConfig
		.keySet()
		.parallelStream()
		.filter(v2 -> ! v.equals(entityClassConfig.get(v2)))
		.forEach(e -> verifyRelation (theGraph , v , entityClassConfig.get(e)));
		return true;
	}


	private  void verifyRelation(Graph<Class<?> , MetaEdge>  theGraph, Class<?> v, Class<?> v1) {
			Class<T> cls1 = (Class<T>)v;
			Class<T> cls2 = (Class<T>)v1;
			verifyForeignKeys(entityGraph , Optional.ofNullable(cls1) , Optional.ofNullable(cls2) );
			processeRelationField( theGraph, v,v1);
	}
	
	private void processeRelationField(Graph<Class<?> , MetaEdge>  theGraph, Class<?> v, Class<?> v1) {
		if(!theGraph.containsVertex(v1)){
			log.info("vertex1 added {}" , v1 );
			theGraph.addVertex(v1);
		}
		if (!v.equals(v1))
			Arrays.asList(v.getDeclaredFields()).parallelStream()
			.filter(f -> f.getAnnotation(javax.persistence.Id.class) !=null)
			.forEach(f -> {
				Arrays.asList(v1.getDeclaredFields()).parallelStream()
				.forEach(f1 -> {
					if(hasFieldEquivalence(f,f1)){
						AtomicInteger ai = new AtomicInteger();
						synchronized(ai) {
							ai.getAndAdd(1);
							Object edge = theGraph.addEdge(v, v1 , new MetaEdge(v , v1));
							log.info("the edge {}:" , edge);
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
		String fieldName = field.getDeclaringClass().getSimpleName() + "id";
		String fieldName1 = f1.getDeclaringClass().getSimpleName()+"id";
		return fieldName.toLowerCase().equals(f1.getName().toLowerCase()) || fieldName1.toLowerCase().contains(field.getDeclaringClass().getSimpleName()); 
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
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object prepareGraphPayload(Object m) {
		SearchParameters parameters = SearchParameters.class.cast(m);
		Map<String,Object> payload = new HashMap<>();
		List<Class<?>> visited = new ArrayList<>();
		payload.put("visited", visited);
		String inputClass1 = parameters.getInputClass1();
		payload.put("inputClass1", inputClass1);
		Optional<Class<T>> clazz1 = createClass(inputClass1);
		String inputClass2 = parameters.getInputClass2();
		payload.put("inputClass2", inputClass2);
		Optional<Class<T>> clazz2 = createClass(inputClass2);
		payload.put("graph", mountRelationGraph());
		payload.put("parameters", parameters.getParameters());
		DijkstraShortestPath<Class<?> , MetaEdge> bf = new DijkstraShortestPath<>(checkGraph(mountRelationGraph() , payload , clazz1 , clazz2));
		GraphPath<Class<?> , MetaEdge> g1 = bf.getPath(clazz1.get(),clazz2.get());
		payload.put("path" , g1);
		return payload;	
	}
	

	private Graph<Class<?>, MetaEdge>  checkGraph(Graph<Class<?>, MetaEdge> entityGraph, Map payload, Optional<Class<T>> clazz1,
			Optional<Class<T>> clazz2) {
		if (payload.get("simple") !=null) {
			Graph<Class<?> , MetaEdge> theGraph = GraphTypeBuilder.<Class<?>, MetaEdge> undirected().allowingMultipleEdges(false)
					.allowingSelfLoops(true).edgeClass(MetaEdge.class).weighted(false).buildGraph();
			theGraph.addVertex(clazz1.get());
			if(!theGraph.containsVertex(clazz2.get())){
				theGraph.addVertex(clazz2.get());
				theGraph.addEdge(clazz1.get(), clazz2.get());
			}else {
				theGraph.addEdge(clazz1.get(), clazz1.get());
			}
			return theGraph;
		}
		return entityGraph;
	}
	
	private void verifyForeignKeys(Graph<Class<?>, MetaEdge> entityGraph2, Optional<Class<T>> clazz1,
			Optional<Class<T>> clazz2) {
		clazz2.ifPresent(c ->{
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
							entityGraph2.addEdge(clazz1.get(), clazz1.get());
						}
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
		String inputClass1 = String.class.cast(payload.get("inputClass1"));
		GraphPath<Class<?> , MetaEdge> g1 = GraphPath.class.cast(payload.get("path"));
		Class<?> clazz1 = createClass(inputClass1).get();
		String inputClass2 = String.class.cast(payload.get("inputClass2"));
		Class<?> clazz2 = createClass(inputClass2).get();
		Object msg1 =  processaClassePayload(m, inputClass1);
		Map orElse = Map.class.cast(msg1);
		getNextPath(m , g1 , Optional.of(clazz1)).ifPresentOrElse(c -> {
			addVisited(clazz1 , msg1); 
			processNextStep(Map.class.cast(msg1) , g1 , c);
		}, () -> { orElse.put("inputClass1", Class.class.cast(clazz2).getName() ); log.info("what is happening? {} " , orElse);});
		return msg1;		
	}
	
	private void processNextStep(Map payload, GraphPath<Class<?>, MetaEdge> g1, Object c) {
		payload.put("inputClass1", Class.class.cast(c).getName());
		payload.put("nextStep", c);
	}
	
	
	private Class addVisited(Class<?> c , Object m) {
		Map payload = Map.class.cast(m);
		List<Class<?>> visited = List.class.cast(payload.get("visited"));
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
		List<Class<?>> visited = List.class.cast(payload.get("visited"));
		Class<?> nextStep = (Class<?>) payload.get("nextStep");

		return Optional.ofNullable(nextStep)
				.map(n -> {
					GraphPath<Class<?>, MetaEdge> path = GraphPath.class.cast(payload.get("path"));
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
			List<?> result = prepareRepository(instance, payload);
			Map<String,Object> newPayload = filterPayload(payload);
			return processNode(objectMapper.convertValue(result, JsonNode.class) , newPayload , Class.class.cast(instance.getClass()));
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
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

	
	private <P> void populateInstanceToPayload(P pojo, Map payload) {
		Pattern exclusionPattern = Pattern.compile("[$]");
		WrapDynaBean bean = new WrapDynaBean(pojo);
		Arrays.asList(pojo.getClass().getDeclaredFields())
		.forEach(f -> {
			if (!exclusionPattern.matcher(f.getName()).find()) {
					String className = f.getDeclaringClass().getSimpleName().toLowerCase();
					String fieldName = f.getName().toLowerCase();
					payload.put(className+"." + fieldName, bean.get(f.getName()));
			}
		});
		
	}
	
	public List<T> prepareRepository(T instance , Map<String,Object> payload){
		ExampleMatcher matcher = ExampleMatcher.matchingAll().withStringMatcher(StringMatcher.CONTAINING). withIgnoreCase().withIgnoreNullValues();
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
		return instancePopulator.populate(instance, payload);
	}

	private Optional<?> getNextPath(Object m , GraphPath<Class<?>, MetaEdge> g1, Optional<Class<?>> clazzz) {
		return clazzz.map(c -> {
			Map payload = Map.class.cast(m);
			List<Class<?>> visited = List.class.cast(payload.get("visited"));
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

