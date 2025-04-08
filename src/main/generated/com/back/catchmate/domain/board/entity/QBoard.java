package com.back.catchmate.domain.board.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBoard is a Querydsl query type for Board
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBoard extends EntityPathBase<Board> {

    private static final long serialVersionUID = 1729688709L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBoard board = new QBoard("board");

    public final com.back.catchmate.global.entity.QBaseTimeEntity _super = new com.back.catchmate.global.entity.QBaseTimeEntity(this);

    public final ListPath<BookMark, QBookMark> bookMarkList = this.<BookMark, QBookMark>createList("bookMarkList", BookMark.class, QBookMark.class, PathInits.DIRECT2);

    public final com.back.catchmate.domain.chat.entity.QChatRoom chatRoom;

    public final com.back.catchmate.domain.club.entity.QClub club;

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> currentPerson = createNumber("currentPerson", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final ListPath<com.back.catchmate.domain.enroll.entity.Enroll, com.back.catchmate.domain.enroll.entity.QEnroll> enrollList = this.<com.back.catchmate.domain.enroll.entity.Enroll, com.back.catchmate.domain.enroll.entity.QEnroll>createList("enrollList", com.back.catchmate.domain.enroll.entity.Enroll.class, com.back.catchmate.domain.enroll.entity.QEnroll.class, PathInits.DIRECT2);

    public final com.back.catchmate.domain.game.entity.QGame game;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isCompleted = createBoolean("isCompleted");

    public final DateTimePath<java.time.LocalDateTime> liftUpDate = createDateTime("liftUpDate", java.time.LocalDateTime.class);

    public final NumberPath<Integer> maxPerson = createNumber("maxPerson", Integer.class);

    public final ListPath<com.back.catchmate.domain.notification.entity.Notification, com.back.catchmate.domain.notification.entity.QNotification> notificationList = this.<com.back.catchmate.domain.notification.entity.Notification, com.back.catchmate.domain.notification.entity.QNotification>createList("notificationList", com.back.catchmate.domain.notification.entity.Notification.class, com.back.catchmate.domain.notification.entity.QNotification.class, PathInits.DIRECT2);

    public final StringPath preferredAgeRange = createString("preferredAgeRange");

    public final StringPath preferredGender = createString("preferredGender");

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.back.catchmate.domain.user.entity.QUser user;

    public QBoard(String variable) {
        this(Board.class, forVariable(variable), INITS);
    }

    public QBoard(Path<? extends Board> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBoard(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBoard(PathMetadata metadata, PathInits inits) {
        this(Board.class, metadata, inits);
    }

    public QBoard(Class<? extends Board> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chatRoom = inits.isInitialized("chatRoom") ? new com.back.catchmate.domain.chat.entity.QChatRoom(forProperty("chatRoom"), inits.get("chatRoom")) : null;
        this.club = inits.isInitialized("club") ? new com.back.catchmate.domain.club.entity.QClub(forProperty("club")) : null;
        this.game = inits.isInitialized("game") ? new com.back.catchmate.domain.game.entity.QGame(forProperty("game"), inits.get("game")) : null;
        this.user = inits.isInitialized("user") ? new com.back.catchmate.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

