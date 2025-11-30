package com.back.catchmate.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 821503861L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUser user = new QUser("user");

    public final com.back.catchmate.global.entity.QBaseTimeEntity _super = new com.back.catchmate.global.entity.QBaseTimeEntity(this);

    public final ComparablePath<Character> allAlarm = createComparable("allAlarm", Character.class);

    public final EnumPath<Authority> authority = createEnum("authority", Authority.class);

    public final DatePath<java.time.LocalDate> birthDate = createDate("birthDate", java.time.LocalDate.class);

    public final ListPath<com.back.catchmate.domain.board.entity.Board, com.back.catchmate.domain.board.entity.QBoard> boardList = this.<com.back.catchmate.domain.board.entity.Board, com.back.catchmate.domain.board.entity.QBoard>createList("boardList", com.back.catchmate.domain.board.entity.Board.class, com.back.catchmate.domain.board.entity.QBoard.class, PathInits.DIRECT2);

    public final ComparablePath<Character> chatAlarm = createComparable("chatAlarm", Character.class);

    public final com.back.catchmate.domain.club.entity.QClub club;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath email = createString("email");

    public final ComparablePath<Character> enrollAlarm = createComparable("enrollAlarm", Character.class);

    public final ListPath<com.back.catchmate.domain.enroll.entity.Enroll, com.back.catchmate.domain.enroll.entity.QEnroll> enrollList = this.<com.back.catchmate.domain.enroll.entity.Enroll, com.back.catchmate.domain.enroll.entity.QEnroll>createList("enrollList", com.back.catchmate.domain.enroll.entity.Enroll.class, com.back.catchmate.domain.enroll.entity.QEnroll.class, PathInits.DIRECT2);

    public final ComparablePath<Character> eventAlarm = createComparable("eventAlarm", Character.class);

    public final StringPath fcmToken = createString("fcmToken");

    public final ComparablePath<Character> gender = createComparable("gender", Character.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isReported = createBoolean("isReported");

    public final StringPath nickName = createString("nickName");

    public final StringPath profileImageUrl = createString("profileImageUrl");

    public final EnumPath<Provider> provider = createEnum("provider", Provider.class);

    public final StringPath providerId = createString("providerId");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final ListPath<com.back.catchmate.domain.chat.entity.UserChatRoom, com.back.catchmate.domain.chat.entity.QUserChatRoom> userChatRoomList = this.<com.back.catchmate.domain.chat.entity.UserChatRoom, com.back.catchmate.domain.chat.entity.QUserChatRoom>createList("userChatRoomList", com.back.catchmate.domain.chat.entity.UserChatRoom.class, com.back.catchmate.domain.chat.entity.QUserChatRoom.class, PathInits.DIRECT2);

    public final StringPath watchStyle = createString("watchStyle");

    public QUser(String variable) {
        this(User.class, forVariable(variable), INITS);
    }

    public QUser(Path<? extends User> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUser(PathMetadata metadata, PathInits inits) {
        this(User.class, metadata, inits);
    }

    public QUser(Class<? extends User> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.club = inits.isInitialized("club") ? new com.back.catchmate.domain.club.entity.QClub(forProperty("club")) : null;
    }

}

