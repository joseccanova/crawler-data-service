package org.nanotek.crawler.data.mongo.repositories;

import org.nanotek.crawler.data.domain.mongodb.ClassPayloadDefinition;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;

public interface ClassPayloadDefinitionRepository
		extends CrudRepository<ClassPayloadDefinition, String>, QuerydslPredicateExecutor<ClassPayloadDefinition> {

}
