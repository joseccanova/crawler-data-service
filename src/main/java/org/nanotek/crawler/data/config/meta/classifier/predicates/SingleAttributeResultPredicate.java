package org.nanotek.crawler.data.config.meta.classifier.predicates;

import java.util.Optional;
import java.util.function.Predicate;

import org.nanotek.crawler.data.config.meta.classifier.IdentityResult;
import org.nanotek.crawler.data.config.meta.classifier.IdentityResult.IdentityType;

public class SingleAttributeResultPredicate implements Predicate<IdentityResult>{

	public SingleAttributeResultPredicate() {
	}

	@Override
	public boolean test(IdentityResult t) {
		return Optional.ofNullable(t).map(r ->  r.getType().equals(IdentityResult.IdentityType.Single)).orElse(false);
	}

}
