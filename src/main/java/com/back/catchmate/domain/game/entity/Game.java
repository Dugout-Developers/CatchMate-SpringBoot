package com.back.catchmate.domain.game.entity;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "games")
public class Game extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id")
    private Long id;

    @Column(nullable = false)
    private LocalDateTime gameStartDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_club_id", nullable = false)
    private Club homeClub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_club_id", nullable = false)
    private Club awayClub;

    @Column(nullable = false)
    private String location;

    @Builder.Default
    @OneToMany(mappedBy = "game")
    private List<Board> boardList = new ArrayList<>();

    public void updateGame(Club homeClub, Club awayClub, LocalDateTime gameStartDate) {
        this.homeClub = homeClub;
        this.awayClub = awayClub;
        this.gameStartDate = gameStartDate;
    }
}
