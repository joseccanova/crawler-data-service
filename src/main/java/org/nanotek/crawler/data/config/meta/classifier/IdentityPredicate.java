package org.nanotek.crawler.data.config.meta.classifier;

import java.util.function.Predicate;

import org.nanotek.crawler.data.config.meta.MetaIdentity;

public class IdentityPredicate implements Predicate<MetaIdentity> {

	@Override
	public boolean test(MetaIdentity t) {
		return false;
	}

}
