package com.back.catchmate.domain.game.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGame is a Querydsl query type for Game
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGame extends EntityPathBase<Game> {

    private static final long serialVersionUID = 2077222211L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGame game = new QGame("game");

    public final com.back.catchmate.global.entity.QBaseTimeEntity _super = new com.back.catchmate.global.entity.QBaseTimeEntity(this);

    public final com.back.catchmate.domain.club.entity.QClub awayClub;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final DateTimePath<java.time.LocalDateTime> gameStartDate = createDateTime("gameStartDate", java.time.LocalDateTime.class);

    public final com.back.catchmate.domain.club.entity.QClub homeClub;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath location = createString("location");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QGame(String variable) {
        this(Game.class, forVariable(variable), INITS);
    }

    public QGame(Path<? extends Game> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGame(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGame(PathMetadata metadata, PathInits inits) {
        this(Game.class, metadata, inits);
    }

    public QGame(Class<? extends Game> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.awayClub = inits.isInitialized("awayClub") ? new com.back.catchmate.domain.club.entity.QClub(forProperty("awayClub")) : null;
        this.homeClub = inits.isInitialized("homeClub") ? new com.back.catchmate.domain.club.entity.QClub(forProperty("homeClub")) : null;
    }

}

