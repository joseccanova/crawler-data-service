package org.nanotek.crawler.data.config.meta.classifier;

import java.util.Optional;

import org.nanotek.crawler.data.config.meta.classifier.IdentityResult.IdentityType;

import schemacrawler.schema.PrimaryKey;

public class PrimaryKeyClassifier implements Classifier<PrimaryKey ,Optional<IdentityResult>>{

	@Override
	public Optional<IdentityResult> classify(PrimaryKey pk) {
		Optional<IdentityResult> ir =  Optional
					.ofNullable(getIdentityType(pk))
					.map(t -> new IdentityResult(t));
		processKeyAttributes(ir); 
		return ir;
	}

	private void processKeyAttributes(Optional<IdentityResult> ir) {
		
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
