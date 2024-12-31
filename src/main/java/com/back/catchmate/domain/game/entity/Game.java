package com.back.catchmate.domain.game.entity;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.joda.time.DateTime;

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

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "home_club_id", nullable = false)
    Club homeClub;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "away_club_id", nullable = false)
    Club awayClub;

    @Column(nullable = false)
    private String location;

    @Builder.Default
    @OneToMany(mappedBy = "game")
    private List<Board> boardList = new ArrayList<>();
}
