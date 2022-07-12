package org.nanotek.crawler.data.mongo.repositories;

import org.nanotek.crawler.data.domain.mongodb.PayloadAttribute;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;

public interface PayloadAttributeRepository extends CrudRepository<PayloadAttribute, String>, QuerydslPredicateExecutor<PayloadAttribute> {

}
