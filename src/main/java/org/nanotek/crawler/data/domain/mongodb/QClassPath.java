package org.nanotek.crawler.data.domain.mongodb;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QClassPath is a Querydsl query type for ClassPath
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QClassPath extends EntityPathBase<ClassPath> {

    private static final long serialVersionUID = 1382100047L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QClassPath classPath = new QClassPath("classPath");

    public final QIdentity _super = new QIdentity(this);

    //inherited
    public final StringPath id = _super.id;

    public final StringPath pathName = createString("pathName");

    public final QClassPayloadDefinition payloadDefinition;

    public QClassPath(String variable) {
        this(ClassPath.class, forVariable(variable), INITS);
    }

    public QClassPath(Path<? extends ClassPath> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QClassPath(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QClassPath(PathMetadata metadata, PathInits inits) {
        this(ClassPath.class, metadata, inits);
    }

    public QClassPath(Class<? extends ClassPath> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.payloadDefinition = inits.isInitialized("payloadDefinition") ? new QClassPayloadDefinition(forProperty("payloadDefinition")) : null;
    }

}

