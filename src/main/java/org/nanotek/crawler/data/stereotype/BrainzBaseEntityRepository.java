package org.nanotek.crawler.data.stereotype;

import org.nanotek.crawler.BaseEntity;

public interface BrainzBaseEntityRepository< T extends BaseEntity<ID> , ID> extends EntityRepository<T, ID> {
}
