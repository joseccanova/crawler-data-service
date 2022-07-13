package org.nanotek.crawler.data;

import java.util.Map;

import org.jgrapht.Graph;
import org.nanotek.crawler.data.config.meta.MetaEdge;
import org.nanotek.crawler.data.config.meta.TempClass;
import org.nanotek.crawler.service.GraphRelationsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@RestController
@RequestMapping(path="/model_relations")
public class GraphController {

	
	@Autowired
	GraphRelationsConfig<?,?> relationService;
	
	@GetMapping(path="/classes")
	public ResponseEntity<Map<?,?>> getClasses(){
		return ResponseEntity.ok(relationService.getEntityClassConfig());
	}
	
	@GetMapping(path="/graph")
	public ResponseEntity<?> getRelations(){
		Graph<Class<?>, ?> relations = relationService.mountRelationGraph();
		return ResponseEntity.ok().body(relations.edgeSet());
	}
	
	@GetMapping(path="/path")
	public ResponseEntity<?> getPath(@RequestBody JsonNode jsonNode){
		return ResponseEntity.ok(relationService.getGraphPath(jsonNode) );
	}
	
	@GetMapping(path="/paths")
	public ResponseEntity<?> getPaths(@RequestBody JsonNode jsonNode){
		return ResponseEntity.ok(relationService.getGraphPaths(jsonNode) );
	}
	
	@GetMapping(path="/repositories")
	public ResponseEntity<?> getPath(){
		return ResponseEntity.ok(relationService.getRepositories());
	}
	
	@PostMapping(path="/add_invalid_edge")
	public ResponseEntity<MetaEdge> addInvalidEdge(@RequestBody JsonNode node){
		TempClass tempClass = objectMapper.convertValue(node, TempClass.class);
		String cls1 = tempClass.getSource();
		String cls2 = tempClass.getTarget();
		Class<?> cl = (Class<?>) relationService.getEntityClassConfig().get(cls1);
		Class<?> c2 = (Class<?>) relationService.getEntityClassConfig().get(cls2);
		MetaEdge me = new MetaEdge(cl, c2);
		if(!relationService.getInvalidEdges().contains(me)) {
			relationService.getInvalidEdges().add(me);
		}
		return ResponseEntity.ok(me);
	}
	
	@PostMapping(path="/add_invalid_vertex")
	public ResponseEntity<Class<?>> addInvalidVertex(@RequestBody JsonNode node){
		TempClass tempClass = objectMapper.convertValue(node, TempClass.class);
		String cls1 = tempClass.getSource();
		Class<?> cl = (Class<?>) relationService.getEntityClassConfig().get(cls1);
		if(!relationService.getInvalidVertex().contains(cl)) {
			relationService.getInvalidVertex().add(cl);
		}
		return ResponseEntity.ok(cl);
	}
	
	@Autowired
	@Qualifier("inputChannel")
	MessageChannel inputChannel;
	
	@Autowired
	@Qualifier("inputChannelTemplate")
	MessagingTemplate inputTemplate;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@GetMapping(path="/search")
	ResponseEntity<?> processSearch(@RequestBody SearchParameters parameters){
		Message<?> msg = MessageBuilder.withPayload(parameters).build();
		inputTemplate.setDefaultChannel(inputChannel);
		Message<?> reply = inputTemplate.sendAndReceive(msg);
		return ResponseEntity.ok(reply);
	}
	
}
