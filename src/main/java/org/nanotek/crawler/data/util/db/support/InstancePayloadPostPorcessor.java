package org.nanotek.crawler.data.util.db.support;

import java.util.Map;

public interface InstancePayloadPostPorcessor<I> {

	void verifyInstance (I instance ,  Map<String,Object> payload);
}
