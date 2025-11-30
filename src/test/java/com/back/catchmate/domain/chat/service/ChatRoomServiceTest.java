package com.back.catchmate.domain.chat.service;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.repository.BoardRepository;
import com.back.catchmate.domain.chat.dto.ChatResponse.ChatRoomInfo;
import com.back.catchmate.domain.chat.dto.ChatResponse.PagedChatRoomInfo;
import com.back.catchmate.domain.chat.entity.ChatRoom;
import com.back.catchmate.domain.chat.entity.UserChatRoom;
import com.back.catchmate.domain.chat.repository.ChatMessageRepository;
import com.back.catchmate.domain.chat.repository.ChatRoomRepository;
import com.back.catchmate.domain.chat.repository.UserChatRoomRepository;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.enroll.entity.Enroll;
import com.back.catchmate.domain.enroll.repository.EnrollRepository;
import com.back.catchmate.domain.game.entity.Game;
import com.back.catchmate.domain.game.repository.GameRepository;
import com.back.catchmate.domain.user.entity.Authority;
import com.back.catchmate.domain.user.entity.Provider;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.error.exception.BaseException;
import com.back.catchmate.global.s3.S3Service;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ChatRoomServiceTest {

    @Autowired private ChatRoomService chatRoomService;
    @Autowired private ChatRoomRepository chatRoomRepository;
    @Autowired private UserChatRoomRepository userChatRoomRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BoardRepository boardRepository;
    @Autowired private ClubRepository clubRepository;
    @Autowired private GameRepository gameRepository;
    @Autowired private EnrollRepository enrollRepository;
    @Autowired private EntityManager em;

    // MongoDB 및 외부 서비스는 Mocking
    @MockBean private ChatMessageRepository chatMessageRepository;
    @MockBean private S3Service s3Service;
    @MockBean private ChatService chatService;

    private User owner;
    private User participant;
    private Club club;
    private Board board;
    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        // 데이터 초기화
        club = clubRepository.save(createClub());
        owner = userRepository.save(createUser("owner@test.com", "Owner"));
        participant = userRepository.save(createUser("participant@test.com", "Participant"));

        Game game = gameRepository.save(createGame(club));
        board = boardRepository.save(createBoard(owner, club, game));

        // 채팅방 생성 및 방장 입장
        chatRoom = chatRoomRepository.save(ChatRoom.builder()
                .board(board)
                .participantCount(1)
                .build());

        userChatRoomRepository.save(UserChatRoom.builder()
                .user(owner)
                .chatRoom(chatRoom)
                .isNewChatRoom(true)
                .joinedAt(LocalDateTime.now())
                .build());

        // [수정] 영속성 컨텍스트 초기화 (Board 엔티티에 ChatRoom 연관관계를 반영하기 위해)
        em.flush();
        em.clear();

        // 초기화 후 객체 다시 조회 (영속 상태 유지)
        board = boardRepository.findById(board.getId()).orElseThrow();
        chatRoom = chatRoomRepository.findById(chatRoom.getId()).orElseThrow();
        owner = userRepository.findById(owner.getId()).orElseThrow();
        participant = userRepository.findById(participant.getId()).orElseThrow();
    }

    @Test
    @DisplayName("채팅방 목록 조회 성공 - 참여 중인 채팅방이 조회되어야 한다")
    void getChatRoomList_Success() {
        // given
        // Mocking: 읽지 않은 메시지 수 (MongoDB)
        given(chatMessageRepository.countByChatRoomIdAndSendTimeGreaterThanAndMessageType(any(), any(), any()))
                .willReturn(5L);

        // when
        PagedChatRoomInfo result = chatRoomService.getChatRoomList(owner.getId(), PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getChatRoomInfoList().get(0).getUnreadMessageCount()).isEqualTo(5);
        assertThat(result.getChatRoomInfoList().get(0).getChatRoomId()).isEqualTo(chatRoom.getId());
    }

    @Test
    @DisplayName("채팅방 단건 조회 성공 - 참여자여야 조회 가능하다")
    void getChatRoom_Success() {
        // given
        given(chatMessageRepository.countByChatRoomIdAndSendTimeGreaterThanAndMessageType(any(), any(), any()))
                .willReturn(0L);

        // when
        ChatRoomInfo info = chatRoomService.getChatRoom(owner.getId(), chatRoom.getId());

        // then
        assertThat(info.getChatRoomId()).isEqualTo(chatRoom.getId());
    }

    @Test
    @DisplayName("참여하지 않은 채팅방을 조회하면 예외가 발생한다")
    void getChatRoom_Fail_NotParticipant() {
        // when & then
        assertThatThrownBy(() -> chatRoomService.getChatRoom(participant.getId(), chatRoom.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("사용자가 해당 채팅방에 참여하지 않았습니다."); // USER_CHATROOM_NOT_FOUND
    }

    @Test
    @DisplayName("참여자가 채팅방 나가기 성공 - UserChatRoom 삭제, 인원 감소, Enroll 삭제")
    void leaveChatRoom_Participant_Success() {
        // given
        // 참여자 입장 시키기
        userChatRoomRepository.save(UserChatRoom.builder()
                .user(participant)
                .chatRoom(chatRoom)
                .joinedAt(LocalDateTime.now())
                .isNewChatRoom(true)
                .build());
        chatRoom.incrementParticipantCount(); // 1 -> 2
        board.incrementCurrentPerson(); // 1 -> 2 (가정)

        // 참여자의 Enroll 정보 (ACCEPTED 상태)
        enrollRepository.save(Enroll.builder()
                .user(participant).board(board).acceptStatus(AcceptStatus.ACCEPTED).isNew(false).build());

        em.flush();
        em.clear();

        // when
        StateResponse response = chatRoomService.leaveChatRoom(participant.getId(), chatRoom.getId());

        // then
        assertThat(response.isState()).isTrue();

        // 1. UserChatRoom 삭제 확인
        assertThat(userChatRoomRepository.existsByUserIdAndChatRoomIdAndDeletedAtIsNull(participant.getId(), chatRoom.getId())).isFalse();

        // 2. ChatRoom 인원 감소 확인 (2 -> 1)
        ChatRoom updatedChatRoom = chatRoomRepository.findById(chatRoom.getId()).orElseThrow();
        assertThat(updatedChatRoom.getParticipantCount()).isEqualTo(1);

        // 3. Board 인원 감소 확인
        Board updatedBoard = boardRepository.findById(board.getId()).orElseThrow();
        assertThat(updatedBoard.getCurrentPerson()).isEqualTo(1); // 초기값 1(방장)

        // 4. Enroll 삭제 확인
        assertThat(enrollRepository.findByUserIdAndBoardIdAndDeletedAtIsNull(participant.getId(), board.getId())).isEmpty();
    }

    @Test
    @DisplayName("방장이 채팅방 나가기 성공 - 게시글 및 채팅방 전체 삭제(Soft Delete)")
    void leaveChatRoom_Owner_Success() {
        // when
        chatRoomService.leaveChatRoom(owner.getId(), chatRoom.getId());

        // then
        // 1. UserChatRoom 삭제 확인
        assertThat(userChatRoomRepository.existsByUserIdAndChatRoomIdAndDeletedAtIsNull(owner.getId(), chatRoom.getId())).isFalse();

        // 2. Board 삭제 확인 (Board 삭제 시 ChatRoom도 로직에 따라 처리됨)
        assertThat(boardRepository.findByIdAndDeletedAtIsNull(board.getId())).isEmpty();
    }

    @Test
    @DisplayName("채팅방 이미지 수정 성공 - 방장만 가능")
    void updateChatRoomImage_Success() throws IOException {
        // given
        MockMultipartFile image = new MockMultipartFile("img", "test.jpg", "image/jpeg", "data".getBytes());
        given(s3Service.uploadFile(any())).willReturn("https://s3/new-image.jpg");

        // when
        chatRoomService.updateChatRoomImage(owner.getId(), chatRoom.getId(), image);

        // then
        ChatRoom updatedRoom = chatRoomRepository.findById(chatRoom.getId()).orElseThrow();
        assertThat(updatedRoom.getChatRoomImage()).isEqualTo("https://s3/new-image.jpg");
    }

    @Test
    @DisplayName("방장이 아닌 유저가 이미지를 수정하려 하면 예외가 발생한다")
    void updateChatRoomImage_Fail_NotOwner() {
        // given
        MockMultipartFile image = new MockMultipartFile("img", "test.jpg", "image/jpeg", "data".getBytes());

        // when & then
        assertThatThrownBy(() -> chatRoomService.updateChatRoomImage(participant.getId(), chatRoom.getId(), image))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("이미지를 수정할 권한이 없습니다."); // IMAGE_UPDATE_UNAUTHORIZED_ACCESS
    }

    @Test
    @DisplayName("유저 강퇴 성공 - 방장만 가능")
    void kickUserFromChatRoom_Success() {
        // given
        // 참여자 입장
        userChatRoomRepository.save(UserChatRoom.builder()
                .user(participant)
                .chatRoom(chatRoom)
                .joinedAt(LocalDateTime.now())
                .isNewChatRoom(true)
                .build());
        chatRoom.incrementParticipantCount();
        board.incrementCurrentPerson();

        em.flush();
        em.clear();

        // when
        StateResponse response = chatRoomService.kickUserFromChatRoom(owner.getId(), chatRoom.getId(), participant.getId());

        // then
        assertThat(response.isState()).isTrue();

        // 참여자 UserChatRoom 삭제 확인
        assertThat(userChatRoomRepository.existsByUserIdAndChatRoomIdAndDeletedAtIsNull(participant.getId(), chatRoom.getId())).isFalse();

        // 인원 감소 확인
        ChatRoom updatedRoom = chatRoomRepository.findById(chatRoom.getId()).orElseThrow();
        assertThat(updatedRoom.getParticipantCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("방장이 아닌 유저가 강퇴를 시도하면 예외가 발생한다")
    void kickUserFromChatRoom_Fail_NotOwner() {
        // when & then
        assertThatThrownBy(() -> chatRoomService.kickUserFromChatRoom(participant.getId(), chatRoom.getId(), owner.getId()))
                .isInstanceOf(BaseException.class)
                // [수정] 실제 발생하는 에러 메시지로 변경
                .hasMessageContaining("채팅방에서 내보낼 권한이 없습니다.");
    }

    @Test
    @DisplayName("채팅방 알림 설정 변경 성공")
    void updateNotificationSetting_Success() {
        // given (기본값: true라고 가정하거나 엔티티 기본값 확인)

        // when
        chatRoomService.updateNotificationSetting(owner.getId(), chatRoom.getId(), false);

        // then
        UserChatRoom ucr = userChatRoomRepository.findByUserIdAndChatRoomIdAndDeletedAtIsNull(owner.getId(), chatRoom.getId()).orElseThrow();
        assertThat(ucr.isNotificationEnabled()).isFalse();
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
                .club(club)
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
                .preferredGender("M")
                .preferredAgeRange("20대")
                .liftUpDate(LocalDateTime.now())
                .build();
    }
}
