package org.nanotek.crawler.data.domain.mongodb;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QIdentity is a Querydsl query type for Identity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QIdentity extends EntityPathBase<Identity> {

    private static final long serialVersionUID = -922144788L;

    public static final QIdentity identity = new QIdentity("identity");

    public final StringPath id = createString("id");

    public QIdentity(String variable) {
        super(Identity.class, forVariable(variable));
    }

    public QIdentity(Path<? extends Identity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QIdentity(PathMetadata metadata) {
        super(Identity.class, metadata);
    }

}

