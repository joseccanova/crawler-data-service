package org.nanotek.crawler.data.config.meta.classifier;

import java.util.Optional;

import org.nanotek.crawler.data.config.meta.classifier.IdentityResult.IdentityType;

import schemacrawler.schema.PrimaryKey;

public class PrimaryKeyClassifier implements Classifier<PrimaryKey ,Optional<IdentityResult>>{

	@Override
	public Optional<IdentityResult> classify(PrimaryKey pk) {
		return Optional
					.ofNullable(pk)
					.map(this::getIdentityResult);
	}

	//TODO: review null type to throw a runtime exception on key classification.
	private IdentityResult getIdentityResult(PrimaryKey k) {
		int size = k.getConstrainedColumns().size();
		if (size == 1) {
			return processIdentityType( IdentityType.Single , k);
		}else if (size > 1) {
			return  processIdentityType( IdentityType.Composite , k);
		}
		return new IdentityResult(IdentityType.Null);
	}


	private IdentityResult processIdentityType(IdentityType type, PrimaryKey k) {
		switch (type) {
		case Single: 
			return processSingleType(IdentityType.Single , k);
		case Composite:
			return processCompositeType(IdentityType.Composite , k);
		default:
			return IdentityResult.NULLRESULT;
		}
	}

	private IdentityResult processCompositeType(IdentityType composite, PrimaryKey k) {
		return null;
	}

	private IdentityResult processSingleType(IdentityType single, PrimaryKey k) {
		return null;
	}

	//TODO: implement the abstraction for the identity result .
	private IdentityResult  processKeyAttributes(IdentityResult.IdentityType it) {
		
		return new IdentityResult(it);
	}
}
