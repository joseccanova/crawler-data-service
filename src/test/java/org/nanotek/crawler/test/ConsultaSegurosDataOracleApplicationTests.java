package org.nanotek.crawler.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jgrapht.Graph;
import org.junit.jupiter.api.Test;
import org.nanotek.crawler.data.util.db.JdbcHelper;
import org.nanotek.crawler.service.GraphRelationsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ConsultaSegurosDataOracleApplicationTests {

	@Autowired 
	JdbcHelper helper; 
	
	@Autowired 
	GraphRelationsConfig<?, ?>  relationsConfig;
	
	@Test
	void contextLoads() {
		assertNotNull(helper);
		assertNotNull(relationsConfig);
	}
	
	@Test
	void testGraphMounting() {
		assertNotNull(relationsConfig);
		Graph g = relationsConfig.mountRelationGraph();
		assertNotNull(g);
	}

}
