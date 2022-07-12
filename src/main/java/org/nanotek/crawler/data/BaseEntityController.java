package org.nanotek.crawler.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.nanotek.crawler.BaseEntity;
import org.nanotek.crawler.SearchContainer;
import org.nanotek.crawler.data.config.meta.IClass;
import org.nanotek.crawler.data.stereotype.EntityBaseRepository;
import org.nanotek.crawler.data.util.Holder;
import org.nanotek.crawler.data.util.db.JdbcHelper;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface BaseEntityController
<B extends BaseEntity<ID> , ID ,  T extends EntityBaseRepository<B , ID> >
extends RepositoryBaseController<T , B> // , SimpleRepresentationModelAssembler<B> 
{

	@PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE} , produces= {MediaType.APPLICATION_JSON_VALUE})
	default ResponseEntity<?> save(@RequestBody JsonNode jsonMap){
		return    populateInstanceFromJsonNode(jsonMap)
						.map(instance -> ResponseEntity.ok().body(saveAndFlush(instance))).orElse(ResponseEntity.notFound().build());
		
	}

	default Class<B> getClazz(){
		return null;
	}
	
	@SuppressWarnings("unchecked")
	default Optional<B> populateInstanceFromJsonNode(JsonNode jsonMap) {
		String myClassName = JdbcHelper.prepareName(getMetaClass().getClassName());
		Holder<B> h  = new Holder<>();
		try {
			B b = (B)	getObjectMapper().treeToValue(jsonMap,getClassesConfig().get(myClassName));
			h.put(b);
		} catch (Exception e2) {
			e2.printStackTrace();
			throw new RuntimeException(e2);
		}
		return h.get();
	}

	@GetMapping(path="/metaclass" ,  produces= {MediaType.APPLICATION_JSON_VALUE})
	default ResponseEntity<IClass> produceMetaClass(){
		return ResponseEntity.ok(getMetaClass());
	}
	
	default ObjectMapper getObjectMapper()
	{ 
		return null;
	};
	
	@PutMapping(consumes = {MediaType.APPLICATION_JSON_VALUE} , produces= {MediaType.APPLICATION_JSON_VALUE})
	default ResponseEntity<?> update(@RequestBody JsonNode jsonMap){
		return populateInstanceFromJsonNode(jsonMap)
				.map(instance -> ResponseEntity.ok().body(saveAndFlush(instance))).orElse(ResponseEntity.notFound().build());
	}
    
//	default B copyProperties(BR baseRepresentation) {
//		Class<B> clazz = (Class<B>) getClassesConfig().get(baseRepresentation.getClass().getSimpleName());
//		try {
//			B instance  = clazz.newInstance();
//			PropertyUtils.copyProperties(instance, baseRepresentation);
//			return instance;
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new RuntimeException(e);
//		}
//	};

	@DeleteMapping(path="/{id}")
	default ResponseEntity<?> delete(@PathVariable(value="id") ID id){
		return ResponseEntity.status(deleteEntity(id)).build();
	}
	
	default HttpStatus deleteEntity(ID b) {
		getRepository()
			.findById(b)
			.ifPresent(e -> getRepository()
			.delete(e));
		
		return HttpStatus.ACCEPTED;
	};

	default <S extends B> S saveAndFlush(S  b) {
		return getRepository().saveAndFlush(b);
	};
	
	@GetMapping(path="/" ,  produces= {MediaType.APPLICATION_JSON_VALUE})
	default ResponseEntity<?> getDataRepoBuddy() throws Exception{
		SearchContainer<B> sc = new SearchContainer<>();
		sc.setEntity(getClazz().newInstance());
		Map<String,Object> sortParameters = new HashMap<>();
		sortParameters.put("start", 0);
		sortParameters.put("pageSize", 1);
		sc.setSortParameters(sortParameters);
		return  ResponseEntity.ok(findByEntityUsingExample(sc));
	}
	
	
	@GetMapping(path = {"/{id}"})
	default ResponseEntity<B> findById(@PathVariable(name = "id")ID id)  {
		 return  ResponseEntity.ok(   getRepository().findById(id).orElseThrow());
	}
	
	@GetMapping(path="/one/{id}" ,  produces= {MediaType.APPLICATION_JSON_VALUE})
	default ResponseEntity<B> getMetaDataClassesById(@PathVariable(name = "id") ID id) throws Exception{
		return  ResponseEntity.ok(getRepository().findById(id).orElseThrow());
	}
	
	
	@GetMapping(path="/example" , consumes = {MediaType.APPLICATION_JSON_VALUE} , produces= {MediaType.APPLICATION_JSON_VALUE})
	default ResponseEntity<?> getMetaDataClassesByExample(@RequestBody JsonNode jsonMap) {
		B b = populateInstanceFromJsonNode(jsonMap).orElseThrow();
		Example<B> queryExample = Example.of(b);
		return  ResponseEntity.ok(getRepository().findAll(queryExample));
	}
	
	@PostMapping(path="/search")
	default ResponseEntity<?> search( @RequestBody JsonNode jsonNode){
		Optional<SearchContainer<B>> bo = populateContainerInstanceFromMap(jsonNode);
		List<?> theList = findByEntityUsingExample(bo.get());
		return ResponseEntity.ok(theList);
	}
	
	@SuppressWarnings("unchecked")
	default Optional<SearchContainer<B>> populateContainerInstanceFromMap(JsonNode jsonNode){
			Holder<SearchContainer<B>> h  = new Holder<>();
			try {
				JsonNode entity = jsonNode.get("entity");
				JsonNode sortParameters = jsonNode.get("sortParameters");
				Class<B> ec = getClazz();
				B sb = 	getObjectMapper().treeToValue(entity,ec);
				Map<String,Object> map = getObjectMapper().treeToValue(sortParameters, Map.class);
				SearchContainer<B> sc = new SearchContainer<B>();
				sc.setEntity(sb);
				sc.setSortParameters(map);
				h.put(sc);
			} catch (Exception e2) {
				e2.printStackTrace();
				throw new RuntimeException(e2);
			}
			return h.get();
	}

	default List<B> getForEntity(String entityName) {
		T  r = getRepository();
		return r.findAll(Example.of(prepareClass(entityName)));
	}
	
	default List<B> findByEntityUsingExample(SearchContainer<B> entityContainer){
		ExampleMatcher matcher = ExampleMatcher
					.matching()
					.withIgnoreCase()
					.withStringMatcher(StringMatcher.CONTAINING)
					.withIgnoreNullValues();
		B entity = entityContainer.getEntity();
		Example<B> ex = Example.of(entity,matcher);
		
		Map<String, Object> sortParameters = entityContainer.getSortParameters();
		
		Integer start = Optional.ofNullable(sortParameters.get("start")).map(v -> Integer.class.cast(v)).orElse(0);
		Integer pageSize = Optional.ofNullable(sortParameters.get("pageSize")).map(v -> Integer.class.cast(v)).orElse(10);
		Pageable pageRequest = Optional
		.ofNullable(entityContainer.getSortParameters())
		.filter(sp -> sp.keySet().size()>0)
		.map(sp ->{
			List<Order> sortOrder = new ArrayList<Order>();
			sp
			.keySet()
			.stream()
			.forEach(k -> {
				String value = sp.get(k).toString();
				if ("ASC".equals(value))
				 	sortOrder.add(Order.asc(k));
				else if ("DESC".equals(value))
				    sortOrder.add(Order.desc(k));
			});
			return PageRequest.of(start , pageSize, Sort.by(sortOrder));
		}).orElse(PageRequest.of(start, pageSize));
		return getRepository().findAll(ex, pageRequest).getContent();
	}
	
	@GetMapping(path="/exampleValue")
	default B createInstance () {
		B b;
		try {
			b = getClazz().newInstance();
			return b;
		} catch (Exception e) {
			throw new NoSuchElementException("error creating class");
		}
	}
	
	@JsonIgnore
	default IClass getMetaClass() {
		return null;
	}
	
}
