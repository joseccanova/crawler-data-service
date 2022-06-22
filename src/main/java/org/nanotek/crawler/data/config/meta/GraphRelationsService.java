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


@Slf4j
@SpringBootConfiguration
@EnableScheduling
public class GraphRelationsService<T extends Base<?,?>> {

	Graph<Class<?>, ?> buRelationGraph; 

	Graph<Class<?>, ?> aciRelationGraph; 

	Graph<Class<?>, ?> acxRelationGraph; 
	
	@Autowired
	@Qualifier("sbuClassCache")
	@Lazy(true)
	Map<Class<?> , String> classCache;
	
	@Autowired
	@Qualifier(value="aciClassCache")
	@Lazy(true)
	Map<Class<?> , String> aciClassCache;

	@Autowired
	@Qualifier(value="tmoranClassCache")
	@Lazy(true)
	Map<Class<?> , String> tmoranClassCache;

	@Autowired
	@Qualifier(value="acxClassCache")
	@Lazy(true)
	Map<Class<?> , String> acxClassCache;

	
	public Map<Class<?>, String> getAciClassCache() {
		return aciClassCache;
	}

	public Map<Class<?>, String> getAcxClassCache() {
		return acxClassCache;
	}

	List<InstancePayloadPostPorcessor<T>> instancePostProcessors = new ArrayList<>();

	public GraphRelationsService() {
		instancePostProcessors.add(new CodigoCorretorPostProcessor<>());
		instancePostProcessors.add(new IdParteEnvolvidaPostProcessor<>());
		instancePostProcessors.add(new AcxIntermediarioCorretorPostProcessor<>());
	}

	public Graph<Class<?>, ?>  mountBuRelationGraph() {
		Graph<Class<?> , ?> theGraph = GraphTypeBuilder.<Class<?>, DefaultEdge> undirected().allowingMultipleEdges(false)
				.allowingSelfLoops(false).edgeClass(MyEdgeClass.class).weighted(false).buildGraph();
		if(buRelationGraph == null) {
			buRelationGraph = theGraph;	
			processClassCache( classCache, buRelationGraph);		
		}
		return buRelationGraph;
	}

	public Graph<Class<?>, ?>  mountAciRelationGraph() {
		Graph<Class<?> , ?> theGraph = GraphTypeBuilder.<Class<?>, DefaultEdge> undirected().allowingMultipleEdges(false)
				.allowingSelfLoops(false).edgeClass(MyEdgeClass.class).weighted(false).buildGraph();
		if (aciRelationGraph == null) {
			aciRelationGraph = theGraph;	
			processClassCache( aciClassCache, aciRelationGraph);		
		}
		return aciRelationGraph;
	}
	
	public Graph<Class<?>, ?>  mountAcxRelationGraph() {
		Graph<Class<?> , ?> theGraph = GraphTypeBuilder.<Class<?>, DefaultEdge> undirected().allowingMultipleEdges(false)
				.allowingSelfLoops(false).edgeClass(MyEdgeClass.class).weighted(false).buildGraph();
		if (acxRelationGraph == null) {
			acxRelationGraph = theGraph;	
			processClassCache( acxClassCache, theGraph);		
		}
		return acxRelationGraph;
	}

	public Graph<Class<?>, ?>  mountParamRelationGraph() {
		Graph<Class<?> , ?> theGraph = GraphTypeBuilder.<Class<?>, DefaultEdge> undirected().allowingMultipleEdges(false)
				.allowingSelfLoops(false).edgeClass(MyEdgeClass.class).weighted(false).buildGraph();
		processClassCache( clientService.getClassCache(), theGraph);	
		return theGraph;
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

	public Map<Class<?> , String> getParamClassCache(){
		return clientService.getClassCache();
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
	void processShortesPath(Graph<Class<?>,MyEdgeClass> theGraph , Class<?> v , Class<?> v1) {
		try {
			BellmanFordShortestPath bf = new BellmanFordShortestPath(theGraph);
			GraphPath g1 = bf.getPath(v, v1);
		}catch(Exception ex) {log.debug("error {}" , ex);}
	}


	@Bean(value="inputClassChannel")
	MessageChannel inputClassChannel(){
		return MessageChannels.direct("inputClassChannel").get();
	}
	
	@Bean(value="inputChannel1")
	MessageChannel inputChannel1(){
		return MessageChannels.direct("inputChannel1").get();
	}

	@Bean(value="inputAciChannel1")
	MessageChannel inputAciChannel1(){
		return MessageChannels.direct("inputAciChannel1").get();
	}

	@Bean(value="inputSbuChannel1")
	MessageChannel inputSbuChannel1(){
		return MessageChannels.direct("inputSbuChannel").get();
	}
	
	@Bean(value="inputAcxChannel")
	MessageChannel inputAcxChannel(){
		return MessageChannels.direct("inputAcxChannel").get();
	}

	@Bean(value="outputChannel")
	MessageChannel outputChannel(){
		return MessageChannels.direct("outputChannel").get();
	}
	
	@Bean(value="inputChannelTemplate")
	@Qualifier(value="inputChannelTemplate")
	MessagingTemplate inputTemplate() {
		return new MessagingTemplate(inputChannel1());
	}

	@Bean(value="inputSbuTemplate")
	@Qualifier(value="inputSbuTemplate")
	MessagingTemplate inputSbuTemplate() {
		return new MessagingTemplate(inputSbuChannel1());
	}
	
	@Bean(value="inputAciTemplate")
	@Qualifier(value="inputAciTemplate")
	MessagingTemplate inputAciTemplate() {
		return new MessagingTemplate(inputAciChannel1());
	}


	@Bean(value="inputAcxTemplate")
	@Qualifier(value="inputAcxTemplate")
	MessagingTemplate inputAcxTemplate() {
		return new MessagingTemplate(inputAcxChannel());
	}
	
	@Bean(value="inputClassTemplate")
	@Qualifier(value="inputClassTemplate")
	MessagingTemplate inputClassTemplate() {
		return new MessagingTemplate(inputAcxChannel());
	}

	@Autowired 
	@Lazy(true)
	DefaultListableBeanFactory beanFactory;

	@Autowired
	@Lazy(true)
	ClientClassesConfig config;

	
	@Bean
	IntegrationFlow createQueryGraphIntegrationFlow(){
		return IntegrationFlows.from(inputClassChannel())
				.transform(m -> {
					return prepareClassGraphPayload(m);
				})
				.channel(outputChannel())
				.get();
	}
	
	@Bean
	IntegrationFlow createInputGraphIntegrationFlow(){
		return IntegrationFlows.from(inputChannel1())
				.transform(m -> {
					return prepareGraphPayload(m);
				})
				.channel(outputChannel())
				.get();
	}

	@Bean
	IntegrationFlow createInputSbuGraphIntegrationFlow(){
		return IntegrationFlows.from(inputSbuChannel1())
				.transform(m -> {
					return prepareSbuGraphPayload(m);
				})
				.channel(outputChannel())
				.get();
	}

	@Bean
	IntegrationFlow createInputAciGraphIntegrationFlow(){
		return IntegrationFlows.from(inputAciChannel1())
				.transform(m -> {
					return prepareAciGraphPayload(m);
				})
				.channel(outputChannel())
				.get();
	}

	@Bean
	IntegrationFlow createInputAcxGraphIntegrationFlow(){
		return IntegrationFlows.from(inputAcxChannel())
				.transform(m -> {
					return prepareAcxGraphPayload(m);
				})
				.channel(outputChannel())
				.get();
	}

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object prepareClassGraphPayload(Object m) {
		Map payload = Map.class.cast(m);
		List<Class<?>> visited = new ArrayList<>();
		payload.put("visited", visited);
		Class<T> clazz1 = Class.class.cast(payload.get("clazz1"));
		Class<T>  clazz2 = Class.class.cast(payload.get("clazz2"));
		payload.put("inputClass1", clazz1.getName());
		payload.put("inputClass2", clazz2.getName());
		Graph<Class<?> , MyEdgeClass> graph = buildSimpleGraph(clazz1 , clazz2);
		DijkstraShortestPath<Class<?> , MyEdgeClass> bf = new DijkstraShortestPath<>(checkGraph(graph , payload , Optional.ofNullable(clazz1) , Optional.ofNullable(clazz2) ));
		GraphPath<Class<?> , MyEdgeClass> g1 = bf.getPath(clazz1,clazz2);
		payload.put("path" , g1);
		return m;	
	}
	
	private Graph<Class<?>, MyEdgeClass> buildSimpleGraph(Class<T> clazz1, Class<T> clazz2) {
			Graph<Class<?> , MyEdgeClass> theGraph = GraphTypeBuilder.<Class<?>, DefaultEdge> undirected().allowingMultipleEdges(false)
					.allowingSelfLoops(true).edgeClass(MyEdgeClass.class).weighted(false).buildGraph();
			
			theGraph.addVertex(clazz1);
			if(!theGraph.containsVertex(clazz2)) {
				theGraph.addVertex(clazz2);
				theGraph.addEdge(clazz1, clazz2);
			}else {
				theGraph.addEdge(clazz1, clazz1);
			}
			
			return theGraph;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object prepareAcxGraphPayload(Object m) {
		Map payload = Map.class.cast(m);
		List<Class<?>> visited = new ArrayList<>();
		payload.put("visited", visited);
		String inputClass1 = String.class.cast(payload.get("inputClass1"));
		Optional<Class<T>> clazz1 = createClass(inputClass1);
		String inputClass2 = String.class.cast(payload.get("inputClass2"));
		Optional<Class<T>> clazz2 = createClass(inputClass2);
		Graph<Class<?> , MyEdgeClass> graph =  (Graph<Class<?>, MyEdgeClass>) mountAcxRelationGraph();
		DijkstraShortestPath<Class<?> , MyEdgeClass> bf = new DijkstraShortestPath<>(checkGraph(graph , payload , clazz1 , clazz2));
		GraphPath<Class<?> , MyEdgeClass> g1 = bf.getPath(clazz1.get(),clazz2.get());
		payload.put("path" , g1);
		return m;	
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object prepareAciGraphPayload(Object m) {
		Map payload = Map.class.cast(m);
		List<Class<?>> visited = new ArrayList<>();
		payload.put("visited", visited);
		String inputClass1 = String.class.cast(payload.get("inputClass1"));
		Optional<Class<T>> clazz1 = createClass(inputClass1);
		String inputClass2 = String.class.cast(payload.get("inputClass2"));
		Optional<Class<T>> clazz2 = createClass(inputClass2);
		Graph<Class<?> , MyEdgeClass> graph =  (Graph<Class<?>, MyEdgeClass>) mountAciRelationGraph();
		DijkstraShortestPath<Class<?> , MyEdgeClass> bf = new DijkstraShortestPath<>(checkGraph(graph , payload , clazz1 , clazz2));
		GraphPath<Class<?> , MyEdgeClass> g1 = bf.getPath(clazz1.get(),clazz2.get());
		payload.put("path" , g1);
		return m;	
	}

	@Bean("sbuGraph")
	@Lazy(true)
	public Graph<Class<?>, MyEdgeClass> sbuGraph(){
		return   (Graph<Class<?>, MyEdgeClass>) mountBuRelationGraph();
	}

	public Graph<Class<?>, MyEdgeClass> getSbuGraph() {
		return sbuGraph();
	}
	
	public Map<Class<?>, String> getClassCache() {
		return classCache;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object prepareSbuGraphPayload(Object m) {
		Map payload = Map.class.cast(m);
		List<Class<?>> visited = new ArrayList<>();
		payload.put("visited", visited);
		String inputClass1 = String.class.cast(payload.get("inputClass1"));
		Optional<Class<T>> clazz1 = createClass(inputClass1);
		String inputClass2 = String.class.cast(payload.get("inputClass2"));
		Optional<Class<T>> clazz2 = createClass(inputClass2);
		DijkstraShortestPath<Class<?> , MyEdgeClass> bf = new DijkstraShortestPath<>(checkGraph(sbuGraph() , payload , clazz1 , clazz2));
		GraphPath<Class<?> , MyEdgeClass> g1 = bf.getPath(clazz1.get(),clazz2.get());
		payload.put("path" , g1);
		return m;	}


	private Graph<Class<?>, MyEdgeClass>  checkGraph(Graph<Class<?>, MyEdgeClass> entityGraph, Map payload, Optional<Class<T>> clazz1,
			Optional<Class<T>> clazz2) {
		if (payload.get("simple") !=null) {
			Graph<Class<?> , MyEdgeClass> theGraph = GraphTypeBuilder.<Class<?>, DefaultEdge> undirected().allowingMultipleEdges(false)
					.allowingSelfLoops(true).edgeClass(MyEdgeClass.class).weighted(false).buildGraph();
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

	Map<Class<?> , String> getSbuClassCache(){
		return classCache;
	}
	
	@Bean("paramGraph")
	@Lazy(true)
	public Graph<Class<?>, MyEdgeClass> paramGraph(){
		return   (Graph<Class<?>, MyEdgeClass>) mountParamRelationGraph();
	}

	public Graph<Class<?>, MyEdgeClass> getParamGraph() {
		return paramGraph();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object prepareGraphPayload(Object m) {
		Map payload = Map.class.cast(m);
		List<Class<?>> visited = new ArrayList<>();
		payload.put("visited", visited);
		String inputClass1 = String.class.cast(payload.get("inputClass1"));
		Optional<Class<T>> clazz1 = createClass(inputClass1);
		String inputClass2 = String.class.cast(payload.get("inputClass2"));
		Optional<Class<T>> clazz2 = createClass(inputClass2);
		if(!paramGraph().containsVertex(clazz2.get()))
			paramGraph().addVertex(clazz2.get());
		DijkstraShortestPath<Class<?> , MyEdgeClass> bf = new DijkstraShortestPath<>(checkGraph(paramGraph() , payload , clazz1 , clazz2));
		GraphPath<Class<?> , MyEdgeClass> g1 = bf.getPath(clazz1.get(),clazz2.get());
		payload.put("path" , g1);
		return m;	
	}


	@SuppressWarnings("unchecked")
	@Bean
	IntegrationFlow createOutputIntegrationFlow(){
		return IntegrationFlows.from(outputChannel())
				.transform(m -> {
					Map payload = Map.class.cast(m);
					return processOutputChannelPayload(m);
				})
				.route(m -> checkEndpath(m))
				.get();
	}

	private Object processOutputChannelPayload(Object m) {
		Map payload = Map.class.cast(m);
		String inputClass1 = String.class.cast(payload.get("inputClass1"));
		GraphPath<Class<?> , MyEdgeClass> g1 = GraphPath.class.cast(payload.get("path"));
		Class<?> clazz1 = createClass(inputClass1).get();
		String inputClass2 = String.class.cast(payload.get("inputClass2"));
		Class<?> clazz2 = createClass(inputClass2).get();
		Object msg1 =  processaClassePayload(m, inputClass1);
		getNextPath(m , g1 , Optional.of(clazz1)).ifPresentOrElse(c -> {
			addVisited(clazz1 , m); 
			processNextStep(payload , g1 , c);
		}, () -> { payload.put("inputClass1", Class.class.cast(clazz2).getName() ); log.info("what is happening? {} " , clazz2);});
		return msg1;		
	}

	@Bean("nillChannel")
	MessageChannel nillChannel() {
		return MessageChannels.direct("nillChannel").get();
	}


	@SuppressWarnings("unchecked")
	@Bean
	IntegrationFlow nillChannelFlow(){
		return IntegrationFlows.from(nillChannel())
				.transform(m -> m)
				.get();
	}

	private MessageChannel checkEndpath(Object m) {
		Map payload = Map.class.cast(m);
		List<Class<?>> visited = List.class.cast(payload.get("visited"));
		Class<?> nextStep = (Class<?>) payload.get("nextStep");

		return Optional.ofNullable(nextStep)
				.map(n -> {
					GraphPath<Class<?>, MyEdgeClass> path = GraphPath.class.cast(payload.get("path"));
					return  visited.contains(nextStep) ?  nillChannel() : outputChannel();
				}).orElse(nillChannel());
	}

	@SuppressWarnings("unchecked")
	private void processNextStep(Map payload, GraphPath<Class<?>, MyEdgeClass> g1, Object c) {
		payload.put("inputClass1", Class.class.cast(c).getName());
		payload.put("nextStep", c);
	}

	private Optional<?> getNextPath(Object m , GraphPath<Class<?>, MyEdgeClass> g1, Optional<Class<?>> clazzz) {
		return clazzz.map(c -> {
			Map payload = Map.class.cast(m);
			List<Class<?>> visited = List.class.cast(payload.get("visited"));
			return g1.getEdgeList().stream()
					.filter(e -> !visited.contains(e.getMySource()))
					.filter(e -> !visited.contains(e.getMyTarget()))
					.map(e -> {
						return processClass(e , c);
					}).findAny().orElse(c);
		});
	}


	private Class addVisited(Class<?> c , Object m) {
		Map payload = Map.class.cast(m);
		List<Class<?>> visited = List.class.cast(payload.get("visited"));
		if (!visited.contains(c))
			visited.add(c);
		return c;
	}

	private Class processClass(MyEdgeClass e, Class<?> c ) {
		return c.equals(e.getMySource())? Class.class.cast(e.getMyTarget()):Class.class.cast(e.getMySource());
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private Optional<Class<T>> createClass(String classStr){
		try {
			return  Optional.ofNullable( (Class<T>) beanFactory.getBeanClassLoader().loadClass(classStr));
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	@SuppressWarnings("unchecked")
	private   Object processaClassePayload(Object m , String classStr) {
		try {
			Class<T> clazzCls= Class.class.cast(beanFactory.getBeanClassLoader().loadClass(classStr));
			Map<String,Object> payload = Map.class.cast(m);
			String url = config.get(clazzCls);
			if (url == null && aciClassCache!=null)
				url = aciClassCache.get(clazzCls);
			if (url == null && classCache !=null)
				url = classCache.get(clazzCls);
			if (url == null && acxClassCache!=null)
				url = acxClassCache.get(clazzCls);
			payload.put("url", url);
			SearchContainer<T> sc = new SearchContainer<>();
			T instance = populateInstance(createIntance(clazzCls) , payload);
			Map<String,Object> params = prepareSortParameters("" , payload);
			sc.setEntity(instance);
			sc.setSortParameters(params);
			postProcessInstance(instance , payload);
			return processaMensagem(m, config, clazzCls,classStr , sc) ;
		}catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Autowired
	@Lazy(true)
	ObjectMapper objectMapper;

	private  void postProcessInstance(T instance, Map<String, Object> payload) {
		instancePostProcessors.stream().forEach(i -> 
		
		{ 
		log.info("instance : {}" , instance);	
		i.verifyInstance(instance, payload);
		});
	}

	@SuppressWarnings("unchecked")
	private  T populateInstance(T correIntance, Map<String, Object> payLoad) {
		Pattern pattern = Pattern.compile("^id|^cd|^nm|^nr|^login|^numid|^cod" , Pattern.CASE_INSENSITIVE);
		WrapDynaBean duna = new WrapDynaBean(correIntance);
		Arrays.asList( correIntance.getClass().getDeclaredFields())
		.stream()
		.forEach(f ->{
			Object value = payLoad.get(f.getName());
			Optional.ofNullable(value)
			.ifPresent(v -> { 
				try {
					if (pattern.matcher(f.getName()).find()) {
						Object converted = ConvertUtils.convert(v, f.getType());
						duna.set(f.getName(), converted);
					}
				}catch (Exception ex) {
					log.info("error on setting property {} " , v );
				}
			});
		});
		postProcessInstance(correIntance, payLoad);
		return (T) duna.getInstance();
	}

	@SuppressWarnings("deprecation")
	private   T createIntance(Class<T> crtorCls) {
		try {
			return crtorCls.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, Object> prepareSortParameters(String string , Map<String,Object> payload) {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("start", 0);
		map.put("pageSize", 2);
		if(payload.get("noSort") !=null)
			map.put("noSort", "noSort");
		return map;
	}

	@Autowired
	@Lazy(true)
	RestTemplate restTemplate;

	@Autowired
	CorretorServiceConfig cooretorServiceConfig;

	@SuppressWarnings("unchecked")
	private   Map<String,Object> processaMensagem(Object m, ClientClassesConfig config, Class<T> usuarioCls, String classStr,
			SearchContainer<T> sc) {
		try {
			Class<T> cls = (Class<T>) beanFactory.getBeanClassLoader().loadClass(classStr);
			Map<String,Object> payLoad = Map.class.cast(m);
			String url =  String.class.cast(payLoad.get("url"));
			return new NodeProcessor<T>(this , restTemplate , objectMapper).processSearchNode(sc , url , cls , payLoad);
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static class NodeProcessor<T extends Base<?>>{

		private RestTemplate restTemplate;
		private ObjectMapper objectMapper;
		GraphRelationsService<T> graphRelationsService;

		public NodeProcessor(GraphRelationsService<T> graphRelationsService, RestTemplate restTemplate2,
				ObjectMapper objectMapper2) {
			this.graphRelationsService = graphRelationsService;
			this.restTemplate = restTemplate2;
			this.objectMapper = objectMapper2;
		}

		public  Map<String, Object> processSearchNode(SearchContainer<T> sc, String url, Class<T> clazz,
				Map<String, Object> payload) {
			T correIntance =   graphRelationsService.populateInstance(graphRelationsService.createIntance(clazz) , payload);
			Map<String,Object> params = graphRelationsService.prepareSortParameters("" , payload);
			sc.setEntity(correIntance);
			sc.setSortParameters(params);
			return processNode ( restTemplate.postForObject(url + "/search", sc , JsonNode.class) , payload , clazz);		
		}


		@SuppressWarnings({ "rawtypes", "unchecked" })
		private <T> void populateInstanceToPayload(T pojo, Map paylod) {
			Pattern exclusionPattern = Pattern.compile("[$]");
			WrapDynaBean bean = new WrapDynaBean(pojo);
			Arrays.asList(pojo.getClass().getDeclaredFields())
			.forEach(f -> {
				if (!exclusionPattern.matcher(f.getName()).find()) {
					paylod.put(f.getName(), bean.get(f.getName()));
				}
			});
		}

		private  <T extends Base<?>> Map<String, Object> processNode(JsonNode returnNode, Map<String, Object> payload , Class<T> clzz) {
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


	}

}

