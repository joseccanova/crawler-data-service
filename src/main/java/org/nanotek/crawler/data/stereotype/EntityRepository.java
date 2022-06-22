package org.nanotek.crawler.data.stereotype;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

public interface EntityRepository<T,ID> extends JpaRepository<T, ID> , QueryByExampleExecutor<T> {
	
	
}