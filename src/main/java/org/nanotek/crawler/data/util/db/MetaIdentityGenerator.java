package org.nanotek.crawler.data.util.db;

import org.nanotek.crawler.data.config.meta.MetaIdentity;
import org.nanotek.crawler.data.config.meta.classifier.IdentityResult;

public interface MetaIdentityGenerator{

	//TODO: implement method for a composite key using bytebuddy
	 static MetaIdentity apply(IdentityResult t) {
		return new MetaIdentity(t.getKey());
	}

}
