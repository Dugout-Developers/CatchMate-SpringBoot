package com.back.catchmate.domain.board.repository;

import com.back.catchmate.domain.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BoardRepositoryCustom {
    Page<Board> findFilteredBoards(Long userId, LocalDate gameDate, Integer maxPerson, List<Long> preferredTeamIdList, Pageable pageable);
}
