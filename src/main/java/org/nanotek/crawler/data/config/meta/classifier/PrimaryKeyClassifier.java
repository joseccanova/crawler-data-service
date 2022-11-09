package org.nanotek.crawler.data.config.meta.classifier;

import java.util.Optional;

import org.nanotek.crawler.data.config.meta.classifier.IdentityResult.IdentityType;

import schemacrawler.schema.PrimaryKey;

public class PrimaryKeyClassifier implements Classifier<PrimaryKey ,Optional<IdentityResult>>{

	@Override
	public Optional<IdentityResult> classify(PrimaryKey pk) {
		Optional<IdentityResult> ir =  Optional
					.ofNullable(getIdentityType(pk))
					.map(t -> processKeyAttributes(t));
		return ir;
	}

	//TODO: implement the abstraction for the identity result .
	private IdentityResult  processKeyAttributes(IdentityResult.IdentityType it) {
		return new IdentityResult(it);
	}

	private IdentityResult.IdentityType getIdentityType(PrimaryKey k) {
		int size = k.getConstrainedColumns().size();
		if (size == 1) {
			return IdentityType.Single;
		}else if (size > 1) {
			return IdentityType.Composite;
		}
		return null;
	}

}
