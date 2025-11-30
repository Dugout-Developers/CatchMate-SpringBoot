package com.back.catchmate.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBlockedUser is a Querydsl query type for BlockedUser
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBlockedUser extends EntityPathBase<BlockedUser> {

    private static final long serialVersionUID = -1431986963L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBlockedUser blockedUser = new QBlockedUser("blockedUser");

    public final com.back.catchmate.global.entity.QBaseTimeEntity _super = new com.back.catchmate.global.entity.QBaseTimeEntity(this);

    public final QUser blocked;

    public final QUser blocker;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QBlockedUser(String variable) {
        this(BlockedUser.class, forVariable(variable), INITS);
    }

    public QBlockedUser(Path<? extends BlockedUser> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBlockedUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBlockedUser(PathMetadata metadata, PathInits inits) {
        this(BlockedUser.class, metadata, inits);
    }

    public QBlockedUser(Class<? extends BlockedUser> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.blocked = inits.isInitialized("blocked") ? new QUser(forProperty("blocked"), inits.get("blocked")) : null;
        this.blocker = inits.isInitialized("blocker") ? new QUser(forProperty("blocker"), inits.get("blocker")) : null;
    }

}

