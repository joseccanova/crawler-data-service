package org.nanotek.crawler.data.config;

import java.util.Optional;

import org.nanotek.crawler.data.domain.mongodb.ClassPath;
import org.nanotek.crawler.data.domain.mongodb.PayloadAttribute;
import org.nanotek.crawler.data.domain.mongodb.QClassPath;
import org.nanotek.crawler.data.mongo.repositories.ClassPathRepository;
import org.nanotek.crawler.data.mongo.repositories.ClassPayloadDefinitionRepository;
import org.nanotek.crawler.data.mongo.repositories.PayloadAttributeRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.querydsl.core.types.SubQueryExpressionImpl;

import lombok.extern.slf4j.Slf4j;

@SpringBootConfiguration(proxyBeanMethods = false)
@EnableMongoRepositories(basePackages = "org.nanotek.crawler.data.mongo.repositories")
@EnableAutoConfiguration
@EnableMongoAuditing
@ComponentScan({"org.nanotek.crawler.data.mongo.repositories" , "org.nanotek.crawler.data.config"}  )
@Slf4j
public class MongoDbConfiguration {

	@Bean
	public MongoClient mongo() {
		ConnectionString connectionString = new ConnectionString("mongodb://admin:admin@localhost:27017/class_graph");
		MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.build();

		return MongoClients.create(mongoClientSettings);
	}

	@Bean
	public MongoTemplate mongoTemplate() throws Exception {
		return new MongoTemplate(mongo(), "class_graph");
	}

	@RestController
	@RequestMapping(path="/class_graph"  , produces=MediaType.APPLICATION_JSON_VALUE)
	public class ClassGraphController  {

		@Autowired 
		ClassPathService service;

		@ResponseBody
		@GetMapping(path="/process")
		ClassPath processClassPath(@RequestBody JsonNode node){
			return service.processClassPath(node);

		}

		@ResponseBody
		@GetMapping(path="/example")
		ClassPath createInstance(){
			return service.createInstance();

		}
		
		@ResponseBody
		@PostMapping(path="/example")
		ClassPath create(@RequestBody JsonNode node){
			return service.create(node);

		}
		
		@ResponseBody
		@PutMapping(path="/example")
		ClassPath updated(@RequestBody JsonNode node){
			return service.save(node);
		}

		@ResponseBody
		@DeleteMapping(path="/by_id/{id}")
		void delete(@PathVariable(name="id") String id){
			 service.delete(id);
		}
		
		@ResponseBody
		@GetMapping(path="/by_id/{id}")
		Optional<ClassPath> find(@PathVariable(name="id") String id){
			return  service.findById(id);
		}
		
		@ResponseBody
		@GetMapping(path="/by_example")
		Iterable<ClassPath> findByExample(@RequestBody JsonNode node ){
			return  service.findByExample(node);
		}
		
		@ResponseBody
		@GetMapping(path="/by_name/{name}")
		Optional<ClassPath> findByName(@PathVariable(name = "name") String name){
			return  service.findByName(name);
		}
		
		@ResponseBody
		@GetMapping(path="/all}")
		Iterable<ClassPath> findAll(){
			return  service.findAll();
		}
	}

	@Service
	@SuppressWarnings("deprecation")
	public  class ClassPathService{

		@Autowired
		ObjectMapper objectMapper;

		@Autowired
		ClassPathRepository classPathRepository;
		
		ClassPath createInstance() {
			try {
				return ClassPath.class.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				log.info("error {} " , e);
				throw new RuntimeException(e);
			}
		}

		public Iterable<ClassPath> findAll() {
			return classPathRepository.findAll();
		}

		public Iterable<ClassPath> findByExample(JsonNode nodePath) {
			ClassPath example = objectMapper.convertValue(nodePath, ClassPath.class);
			return classPathRepository.findAll(QClassPath.classPath.in(example));
		}

		public Optional<ClassPath> findByName(String name) {
			return classPathRepository.findOne(QClassPath.classPath.pathName.eq(name));
		}
		
		public Optional<ClassPath> findById(String id) {
			return classPathRepository.findById(id);
		}

		public void delete(String id) {
			classPathRepository.deleteById(id);
		}

		public ClassPath save(JsonNode node) {
			
			ClassPath updatedPath = processClassPath(node);
			return  classPathRepository
					.findById(updatedPath.getId())
					.map(cp -> copyProperties (cp , updatedPath))
					.map(cp -> classPathRepository.save(cp))
					.orElse(classPathRepository.save(updatedPath));
		}

		private ClassPath copyProperties(ClassPath actual , ClassPath newValues) {
			BeanUtils.copyProperties(newValues , actual);
			return actual;
		}
		
		public ClassPath create(JsonNode node) {
			ClassPath newPath = objectMapper.convertValue(node,ClassPath.class);
			return classPathRepository.save(newPath);
		}

		ClassPath processClassPath(JsonNode pathNode) {
			return objectMapper.convertValue(pathNode,ClassPath.class);
		}
	}
	

}
