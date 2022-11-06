package org.nanotek.crawler.data.config.meta.classifier;

import java.util.Optional;

import org.nanotek.crawler.data.config.meta.classifier.IdentityResult.IdentityType;

import schemacrawler.schema.PrimaryKey;

public class PrimaryKeyClassifier implements Classifier<PrimaryKey , IdentityResult>{

	@Override
	public IdentityResult classify(PrimaryKey pk) {
		return Optional
					.ofNullable(pk)
					.map(k -> getIdentityType(k));
					.map(t -> new IdentityResult(t , pk));
	}

	private IdentityResult.IdentityType getIdentityType(PrimaryKey k) {
		if (k.getConstrainedColumns().size() == 1 )
			return IdentityType.Single;
		else (k.getConstrainedColumns().size() > 1) 
			return IdentityType.Composite;
		return null;
	}

}
