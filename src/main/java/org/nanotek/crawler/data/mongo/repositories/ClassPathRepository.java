package org.nanotek.crawler.data.mongo.repositories;

import org.nanotek.crawler.data.domain.mongodb.ClassPath;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;

public interface ClassPathRepository extends CrudRepository<ClassPath, String>  , QuerydslPredicateExecutor<ClassPath>{
}
