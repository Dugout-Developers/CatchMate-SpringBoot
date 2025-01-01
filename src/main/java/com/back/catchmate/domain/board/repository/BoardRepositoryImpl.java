package com.back.catchmate.domain.board.repository;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.entity.QBoard;
import com.back.catchmate.domain.club.entity.QClub;
import com.back.catchmate.domain.game.entity.QGame;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Board> findFilteredBoards(LocalDate gameDate, Integer maxPerson, Long preferredTeamId, Pageable pageable) {
        QBoard board = QBoard.board;
        QGame game = QGame.game;
        QClub club = QClub.club;

        BooleanBuilder builder = new BooleanBuilder();

        // 삭제되지 않은 게시글만 조회
        builder.and(board.deletedAt.isNull());

        // 저장된 게시글만 조회 (임시 저장 X)
        builder.and(board.isCompleted.isTrue());

        // 최대 인원수 필터
        if (maxPerson != null) {
            builder.and(board.maxPerson.eq(maxPerson));
        }

        // 응원팀 필터
        if (preferredTeamId != null) {
            builder.and(board.club.id.eq(preferredTeamId));
        }

        if (gameDate != null) {
            builder.and(board.game.gameStartDate.goe(gameDate.atStartOfDay()));
            builder.and(board.game.gameStartDate.lt(gameDate.plusDays(1).atStartOfDay()));
        }

        // 쿼리 실행
        JPAQuery<Board> query = queryFactory
                .selectFrom(board)
                .leftJoin(board.club, club).fetchJoin()
                .leftJoin(board.game, game).fetchJoin()
                .where(builder)
                .orderBy(board.createdAt.desc());

        // 페이징 처리
        long total = query.fetchCount();
        List<Board> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }
}
