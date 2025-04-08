package com.back.catchmate.domain.enroll.converter;

import com.back.catchmate.domain.board.converter.BoardConverter;
import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.repository.BoardRepository;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.enroll.dto.EnrollResponse;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.PagedEnrollReceiveInfo;
import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.enroll.entity.Enroll;
import com.back.catchmate.domain.enroll.repository.EnrollRepository;
import com.back.catchmate.domain.game.converter.GameConverter;
import com.back.catchmate.domain.game.entity.Game;
import com.back.catchmate.domain.user.entity.Authority;
import com.back.catchmate.domain.user.entity.Provider;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class EnrollConverterTest {
    @Autowired
    private EnrollConverter enrollConverter;
    @Autowired
    private EnrollRepository enrollRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private BoardConverter boardConverter;
    @Autowired
    private GameConverter gameConverter;

    @Test
    void toPagedEnrollReceiveInfo_ShouldGroupByBoardAndLimitTo10SortedEnrolls() {
        // given
        Club club = createClub();
        User user = createUser(
                "test@test.com",
                "google",
                "1234",
                'M',
                "아아",
                LocalDate.of(1995, 5, 10),
                club,
                "FULL",
                "https://example.com/image.jpg",
                'Y',
                'Y',
                'Y',
                'Y',
                "some-fcm-token"
        );

        Game game = Game.builder()
                .id(1L)
                .awayClub(club)
                .homeClub(club)
                .gameStartDate(LocalDateTime.now())
                .location("광주")
                .build();
        userRepository.save(user);

        Board board1 = toEntity(user, game, club, "제목", "내용", 5, 'F', List.of("20대"), true);
        Board board2 = toEntity(user, game, club, "제목", "내용", 5, 'F', List.of("20대"), true);

        boardRepository.saveAll(List.of(board1, board2));

        // 각각의 보드에 대해 12개의 Enroll 생성 (테스트용)
        List<Enroll> enrolls = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            enrolls.add(createEnroll(Long.parseLong(String.valueOf(i)), "aa", board1, user));
            enrolls.add(createEnroll(Long.parseLong(String.valueOf(i)), "bb", board2, user));
        }

        enrollRepository.saveAll(enrolls);

        Page<Enroll> enrollPage = new PageImpl<>(enrolls, PageRequest.of(0, 10), 24);

        // 실제 로직 수행
        PagedEnrollReceiveInfo result = enrollConverter.toPagedEnrollReceiveInfo(enrollPage);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEnrollInfoList()).hasSize(2); // 2개의 board

        for (EnrollResponse.EnrollReceiveInfo info : result.getEnrollInfoList()) {
            assertThat(info.getEnrollReceiveInfoList()).hasSize(10); // 각 보드당 10개만
        }

        assertThat(result.getTotalElements()).isEqualTo(24);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    public User createUser(
            String email,
            String provider,
            String providerId,
            char gender,
            String nickName,
            LocalDate birthDate,
            Club favoriteClub,
            String watchStyle,
            String profileImageUrl,
            char allAlarm,
            char chatAlarm,
            char enrollAlarm,
            char eventAlarm,
            String fcmToken
    ) {
        return User.builder()
                .email(email)
                .provider(Provider.of(provider))
                .providerId(providerId)
                .gender(gender)
                .nickName(nickName)
                .birthDate(birthDate)
                .club(favoriteClub)
                .watchStyle((watchStyle == null || watchStyle.isEmpty()) ? null : watchStyle)
                .profileImageUrl(profileImageUrl)
                .allAlarm(allAlarm)
                .chatAlarm(chatAlarm)
                .enrollAlarm(enrollAlarm)
                .eventAlarm(eventAlarm)
                .fcmToken(fcmToken)
                .authority(Authority.ROLE_USER)
                .isReported(false)
                .build();
    }


    private Club createClub() {
        return Club.builder()
                .id(1L)
                .name("기아 타이거즈")
                .build();
    }

    public Board createBoard(long id, String title, User user, Club club) {
        return Board.builder()
                .id(id)
                .title(title)
                .user(user)
                .club(club)
                .build();
    }

    public Enroll createEnroll(long id, String description, Board board, User user) {
        return Enroll.builder()
                .id(id)
                .board(board)
                .user(user)
                .acceptStatus(AcceptStatus.PENDING)
                .description(description)
                .isNew(true)
                .build();

    }

    public Board toEntity(
            User user,
            Game game,
            Club cheerClub,
            String title,
            String content,
            Integer maxPerson,
            Character preferredGender,
            List<String> preferredAgeRange,
            Boolean isCompleted
    ) {
        return Board.builder()
                .title(title)
                .content(content)
                .maxPerson(maxPerson)
                .currentPerson(1)
                .user(user)
                .club(cheerClub)
                .game(game)
                .preferredGender(String.valueOf(preferredGender))
                .preferredAgeRange(String.join(",", preferredAgeRange))
                .isCompleted(isCompleted)
                .liftUpDate(LocalDateTime.now())
                .build();
    }

}
