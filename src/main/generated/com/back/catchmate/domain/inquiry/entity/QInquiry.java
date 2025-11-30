package com.back.catchmate.domain.inquiry.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInquiry is a Querydsl query type for Inquiry
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInquiry extends EntityPathBase<Inquiry> {

    private static final long serialVersionUID = -353988059L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInquiry inquiry = new QInquiry("inquiry");

    public final com.back.catchmate.global.entity.QBaseTimeEntity _super = new com.back.catchmate.global.entity.QBaseTimeEntity(this);

    public final StringPath answer = createString("answer");

    public final com.back.catchmate.domain.user.entity.QUser answeredBy;

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<InquiryType> inquiryType = createEnum("inquiryType", InquiryType.class);

    public final BooleanPath isCompleted = createBoolean("isCompleted");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.back.catchmate.domain.user.entity.QUser user;

    public QInquiry(String variable) {
        this(Inquiry.class, forVariable(variable), INITS);
    }

    public QInquiry(Path<? extends Inquiry> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInquiry(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInquiry(PathMetadata metadata, PathInits inits) {
        this(Inquiry.class, metadata, inits);
    }

    public QInquiry(Class<? extends Inquiry> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.answeredBy = inits.isInitialized("answeredBy") ? new com.back.catchmate.domain.user.entity.QUser(forProperty("answeredBy"), inits.get("answeredBy")) : null;
        this.user = inits.isInitialized("user") ? new com.back.catchmate.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

