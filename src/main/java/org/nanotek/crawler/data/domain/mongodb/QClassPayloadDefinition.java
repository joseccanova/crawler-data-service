package org.nanotek.crawler.data.domain.mongodb;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QClassPayloadDefinition is a Querydsl query type for ClassPayloadDefinition
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QClassPayloadDefinition extends EntityPathBase<ClassPayloadDefinition> {

    private static final long serialVersionUID = 652748183L;

    public static final QClassPayloadDefinition classPayloadDefinition = new QClassPayloadDefinition("classPayloadDefinition");

    public final QIdentity _super = new QIdentity(this);

    public final ListPath<PayloadAttribute, QPayloadAttribute> attributeExtractions = this.<PayloadAttribute, QPayloadAttribute>createList("attributeExtractions", PayloadAttribute.class, QPayloadAttribute.class, PathInits.DIRECT2);

    public final ListPath<PayloadAttribute, QPayloadAttribute> attributeFilters = this.<PayloadAttribute, QPayloadAttribute>createList("attributeFilters", PayloadAttribute.class, QPayloadAttribute.class, PathInits.DIRECT2);

    public final StringPath classAlias = createString("classAlias");

    //inherited
    public final StringPath id = _super.id;

    public final StringPath inputClass1 = createString("inputClass1");

    public final StringPath inputClass2 = createString("inputClass2");

    public QClassPayloadDefinition(String variable) {
        super(ClassPayloadDefinition.class, forVariable(variable));
    }

    public QClassPayloadDefinition(Path<? extends ClassPayloadDefinition> path) {
        super(path.getType(), path.getMetadata());
    }

    public QClassPayloadDefinition(PathMetadata metadata) {
        super(ClassPayloadDefinition.class, metadata);
    }

}

