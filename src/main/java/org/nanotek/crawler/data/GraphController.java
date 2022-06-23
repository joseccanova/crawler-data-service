package org.nanotek.crawler.data;

import java.util.Map;

import org.jgrapht.Graph;
import org.nanotek.crawler.service.GraphRelationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(path="/model_relations")
public class GraphController {

	
	@Autowired
	GraphRelationsService<?> relationService;
	
	@GetMapping(path="/classes")
	public ResponseEntity<Map<?,?>> getClasses(){
		return ResponseEntity.ok(relationService.getEntityClassConfig());
	}
	
	@GetMapping(path="/graph")
	public ResponseEntity<?> getRelations(){
		Graph<Class<?>, ?> relations = relationService.mountRelationGraph();
		return ResponseEntity.ok().body(relations.edgeSet());
	}
	
}
