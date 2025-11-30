package com.back.catchmate.domain.chat.service;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.repository.BoardRepository;
import com.back.catchmate.domain.chat.entity.ChatRoom;
import com.back.catchmate.domain.chat.entity.UserChatRoom;
import com.back.catchmate.domain.chat.repository.ChatRoomRepository;
import com.back.catchmate.domain.chat.repository.UserChatRoomRepository;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.game.entity.Game;
import com.back.catchmate.domain.game.repository.GameRepository;
import com.back.catchmate.domain.user.dto.UserResponse.UserInfoList;
import com.back.catchmate.domain.user.entity.Authority;
import com.back.catchmate.domain.user.entity.Provider;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.error.exception.BaseException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserChatRoomServiceTest {

    @Autowired private UserChatRoomService userChatRoomService;
    @Autowired private UserChatRoomRepository userChatRoomRepository;
    @Autowired private ChatRoomRepository chatRoomRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BoardRepository boardRepository;
    @Autowired private ClubRepository clubRepository;
    @Autowired private GameRepository gameRepository;
    @Autowired private EntityManager em;

    // ChatRoomService는 내부적으로 MongoDB 등을 사용하므로 Mocking
    @MockBean private ChatRoomService chatRoomService;

    private User me;
    private User otherUser;
    private ChatRoom myChatRoom;
    private ChatRoom otherChatRoom;

    @BeforeEach
    void setUp() {
        // 1. 기초 데이터 생성
        Club club = clubRepository.save(createClub());
        Game game = gameRepository.save(createGame(club));

        me = userRepository.save(createUser("me@test.com", "Me"));
        otherUser = userRepository.save(createUser("other@test.com", "Other"));

        // 2. 게시글 및 채팅방 생성
        Board board1 = boardRepository.save(createBoard(me, club, game));
        myChatRoom = chatRoomRepository.save(ChatRoom.builder().board(board1).participantCount(2).build());

        Board board2 = boardRepository.save(createBoard(otherUser, club, game));
        otherChatRoom = chatRoomRepository.save(ChatRoom.builder().board(board2).participantCount(1).build());

        // 3. 채팅방 참여 정보(UserChatRoom) 생성
        // - myChatRoom: 나(Me)와 상대방(Other)이 참여 중
        createUserChatRoom(me, myChatRoom);
        createUserChatRoom(otherUser, myChatRoom);

        // - otherChatRoom: 상대방(Other)만 참여 중 (나는 참여 안 함)
        createUserChatRoom(otherUser, otherChatRoom);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("채팅방 참여자 목록 조회 성공 - 내가 속한 채팅방의 모든 유저 정보를 반환해야 한다")
    void getUserInfoList_Success() {
        // when
        UserInfoList result = userChatRoomService.getUserInfoList(me.getId(), myChatRoom.getId());

        // then
        assertThat(result.getUserInfoList()).hasSize(2);
        assertThat(result.getUserInfoList())
                .extracting("nickName")
                .containsExactlyInAnyOrder("Me", "Other");
    }

    @Test
    @DisplayName("참여하지 않은 채팅방의 유저 목록을 조회하면 예외가 발생한다")
    void getUserInfoList_Fail_NotParticipant() {
        // when & then
        // otherChatRoom에는 'Other'만 있고 'Me'는 없음
        assertThatThrownBy(() -> userChatRoomService.getUserInfoList(me.getId(), otherChatRoom.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("사용자가 해당 채팅방에 참여하지 않았습니다."); // USER_CHATROOM_NOT_FOUND
    }

    @Test
    @DisplayName("읽지 않은 채팅이 있으면 True를 반환한다")
    void hasUnreadChat_True() {
        // given
        // myChatRoom에 읽지 않은 메시지가 1개 있다고 가정
        given(chatRoomService.getUnreadMessageCount(eq(me.getId()), eq(myChatRoom.getId())))
                .willReturn(1L);

        // when
        Boolean result = userChatRoomService.hasUnreadChat(me.getId());

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("모든 채팅을 다 읽었으면 False를 반환한다")
    void hasUnreadChat_False() {
        // given
        // 모든 채팅방의 안 읽은 메시지 수가 0이라고 가정
        given(chatRoomService.getUnreadMessageCount(any(), any())).willReturn(0L);

        // when
        Boolean result = userChatRoomService.hasUnreadChat(me.getId());

        // then
        assertThat(result).isFalse();
    }

    // --- Helper Methods ---
    private Club createClub() {
        return Club.builder().name("Test Club").region("Seoul").homeStadium("Stadium").build();
    }

    private User createUser(String email, String nickname) {
        return User.builder()
                .email(email)
                .provider(Provider.GOOGLE)
                .providerId("google_" + email)
                .gender('M')
                .nickName(nickname)
                .birthDate(LocalDate.of(1990, 1, 1))
                .club(clubRepository.findAll().get(0))
                .profileImageUrl("default.jpg")
                .allAlarm('Y').chatAlarm('Y').enrollAlarm('Y').eventAlarm('Y')
                .fcmToken("token_" + email)
                .authority(Authority.ROLE_USER)
                .isReported(false)
                .build();
    }

    private Game createGame(Club club) {
        return Game.builder().homeClub(club).awayClub(club).gameStartDate(LocalDateTime.now()).location("Stadium").build();
    }

    private Board createBoard(User user, Club club, Game game) {
        return Board.builder()
                .user(user)
                .club(club)
                .game(game)
                .title("Title")
                .content("Content")
                .maxPerson(4)
                .currentPerson(1)
                .isCompleted(true)
                .liftUpDate(LocalDateTime.now())
                // [수정] 필수값 추가 (Board 엔티티 정의에 맞게 값 설정)
                .preferredGender("M")
                .preferredAgeRange("20s")
                .build();
    }

    private void createUserChatRoom(User user, ChatRoom chatRoom) {
        userChatRoomRepository.save(UserChatRoom.builder()
                .user(user)
                .chatRoom(chatRoom)
                .isNewChatRoom(false) // 필수값
                .joinedAt(LocalDateTime.now()) // 필수값
                .build());
    }
}
