package com.back.catchmate.domain.enroll.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEnroll is a Querydsl query type for Enroll
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEnroll extends EntityPathBase<Enroll> {

    private static final long serialVersionUID = -918570261L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEnroll enroll = new QEnroll("enroll");

    public final com.back.catchmate.global.entity.QBaseTimeEntity _super = new com.back.catchmate.global.entity.QBaseTimeEntity(this);

    public final EnumPath<AcceptStatus> acceptStatus = createEnum("acceptStatus", AcceptStatus.class);

    public final com.back.catchmate.domain.board.entity.QBoard board;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isNew = createBoolean("isNew");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.back.catchmate.domain.user.entity.QUser user;

    public QEnroll(String variable) {
        this(Enroll.class, forVariable(variable), INITS);
    }

    public QEnroll(Path<? extends Enroll> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEnroll(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEnroll(PathMetadata metadata, PathInits inits) {
        this(Enroll.class, metadata, inits);
    }

    public QEnroll(Class<? extends Enroll> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.board = inits.isInitialized("board") ? new com.back.catchmate.domain.board.entity.QBoard(forProperty("board"), inits.get("board")) : null;
        this.user = inits.isInitialized("user") ? new com.back.catchmate.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

