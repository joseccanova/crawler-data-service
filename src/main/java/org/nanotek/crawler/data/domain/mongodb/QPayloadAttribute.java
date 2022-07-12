package org.nanotek.crawler.data.domain.mongodb;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPayloadAttribute is a Querydsl query type for PayloadAttribute
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPayloadAttribute extends EntityPathBase<PayloadAttribute> {

    private static final long serialVersionUID = 652573148L;

    public static final QPayloadAttribute payloadAttribute1 = new QPayloadAttribute("payloadAttribute1");

    public final QIdentity _super = new QIdentity(this);

    public final ListPath<String, StringPath> aliases = this.<String, StringPath>createList("aliases", String.class, StringPath.class, PathInits.DIRECT2);

    //inherited
    public final StringPath id = _super.id;

    public final StringPath payloadAttribute = createString("payloadAttribute");

    public final StringPath payloadClassString = createString("payloadClassString");

    public QPayloadAttribute(String variable) {
        super(PayloadAttribute.class, forVariable(variable));
    }

    public QPayloadAttribute(Path<? extends PayloadAttribute> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPayloadAttribute(PathMetadata metadata) {
        super(PayloadAttribute.class, metadata);
    }

}

