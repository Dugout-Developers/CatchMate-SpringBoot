package com.back.catchmate.domain.notification.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNotification is a Querydsl query type for Notification
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotification extends EntityPathBase<Notification> {

    private static final long serialVersionUID = -349428299L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNotification notification = new QNotification("notification");

    public final com.back.catchmate.global.entity.QBaseTimeEntity _super = new com.back.catchmate.global.entity.QBaseTimeEntity(this);

    public final EnumPath<com.back.catchmate.domain.enroll.entity.AcceptStatus> acceptStatus = createEnum("acceptStatus", com.back.catchmate.domain.enroll.entity.AcceptStatus.class);

    public final com.back.catchmate.domain.board.entity.QBoard board;

    public final StringPath body = createString("body");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.back.catchmate.domain.inquiry.entity.QInquiry inquiry;

    public final BooleanPath isRead = createBoolean("isRead");

    public final com.back.catchmate.domain.user.entity.QUser sender;

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.back.catchmate.domain.user.entity.QUser user;

    public QNotification(String variable) {
        this(Notification.class, forVariable(variable), INITS);
    }

    public QNotification(Path<? extends Notification> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNotification(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNotification(PathMetadata metadata, PathInits inits) {
        this(Notification.class, metadata, inits);
    }

    public QNotification(Class<? extends Notification> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.board = inits.isInitialized("board") ? new com.back.catchmate.domain.board.entity.QBoard(forProperty("board"), inits.get("board")) : null;
        this.inquiry = inits.isInitialized("inquiry") ? new com.back.catchmate.domain.inquiry.entity.QInquiry(forProperty("inquiry"), inits.get("inquiry")) : null;
        this.sender = inits.isInitialized("sender") ? new com.back.catchmate.domain.user.entity.QUser(forProperty("sender"), inits.get("sender")) : null;
        this.user = inits.isInitialized("user") ? new com.back.catchmate.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

