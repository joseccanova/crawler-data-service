package org.nanotek.crawler.data;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.Graph;
import org.nanotek.crawler.data.config.meta.MetaEdge;
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
	
	
	@GetMapping(path="/graph")
	public ResponseEntity<?> getRelations(){
		Graph<Class<?>, ?> relations = relationService.mountRelationGraph();
		Set<Object> edges = new HashSet<>();
		relations.vertexSet().stream()
		.forEach( v -> {
			relations.vertexSet().stream()
			.forEach(v1 -> {
				Set<?> rel = relations.getAllEdges(v, v1);
				rel.stream()
				.forEach(r -> { 
					MetaEdge ec =  MetaEdge.class.cast(r);
					edges.add(ec.getSource() + " -> " + ec.getTarget());
				});
			});
		} );
		return ResponseEntity.ok().body(edges);
	}
	
}
