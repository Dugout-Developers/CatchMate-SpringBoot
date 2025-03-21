package com.back.catchmate.domain.game.repository;

import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.game.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface GameRepository extends JpaRepository<Game, Long> {
    Game findByHomeClubAndAwayClubAndGameStartDate(Club homeClub, Club awayClub, LocalDateTime gameStartDate);
}
