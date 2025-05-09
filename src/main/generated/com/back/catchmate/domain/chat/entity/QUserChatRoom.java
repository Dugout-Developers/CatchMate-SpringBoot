package com.back.catchmate.domain.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserChatRoom is a Querydsl query type for UserChatRoom
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserChatRoom extends EntityPathBase<UserChatRoom> {

    private static final long serialVersionUID = -360005131L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserChatRoom userChatRoom = new QUserChatRoom("userChatRoom");

    public final com.back.catchmate.global.entity.QBaseTimeEntity _super = new com.back.catchmate.global.entity.QBaseTimeEntity(this);

    public final QChatRoom chatRoom;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isNewChatRoom = createBoolean("isNewChatRoom");

    public final BooleanPath isNotificationEnabled = createBoolean("isNotificationEnabled");

    public final DateTimePath<java.time.LocalDateTime> joinedAt = createDateTime("joinedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> lastReadTime = createDateTime("lastReadTime", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.back.catchmate.domain.user.entity.QUser user;

    public QUserChatRoom(String variable) {
        this(UserChatRoom.class, forVariable(variable), INITS);
    }

    public QUserChatRoom(Path<? extends UserChatRoom> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserChatRoom(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserChatRoom(PathMetadata metadata, PathInits inits) {
        this(UserChatRoom.class, metadata, inits);
    }

    public QUserChatRoom(Class<? extends UserChatRoom> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chatRoom = inits.isInitialized("chatRoom") ? new QChatRoom(forProperty("chatRoom"), inits.get("chatRoom")) : null;
        this.user = inits.isInitialized("user") ? new com.back.catchmate.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

