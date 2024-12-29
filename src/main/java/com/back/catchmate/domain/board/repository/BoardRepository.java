package com.back.catchmate.domain.board.repository;

import com.back.catchmate.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
}
