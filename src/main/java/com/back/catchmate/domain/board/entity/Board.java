package com.back.catchmate.domain.board.entity;

import com.back.catchmate.domain.board.dto.BoardRequest.UpdateBoardRequest;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.enroll.entity.Enroll;
import com.back.catchmate.domain.game.entity.Game;
import com.back.catchmate.domain.notification.entity.Notification;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "boards")
public class Board extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int maxPerson;

    @Column(nullable = false)
    private String preferredGender;

    @Column(nullable = false)
    private String preferredAgeRange;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "board")
    private List<Enroll> enrollList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "board")
    private List<Notification> notificationList = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isCompleted = false;

    public boolean isWriterSameAsLoginUser(User user) {
        return this.user.equals(user);
    }

    public void updateBoard(Club cheerClub, Game game, UpdateBoardRequest boardRequest) {
        this.title = boardRequest.getTitle();
        this.content = boardRequest.getContent();
        this.maxPerson = boardRequest.getMaxPerson();
        this.preferredGender = boardRequest.getPreferredGender();
        this.preferredAgeRange = String.join(",", boardRequest.getPreferredAgeRange());
        this.isCompleted = boardRequest.getIsCompleted();
        this.club = cheerClub;
        this.game = game;
    }
}
