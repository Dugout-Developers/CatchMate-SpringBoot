package com.back.catchmate.domain.board.repository;

import com.back.catchmate.domain.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface BoardRepositoryCustom {
    Page<Board> findFilteredBoards(LocalDate gameDate, Integer maxPerson, Long preferredTeamId, Pageable pageable);
}
