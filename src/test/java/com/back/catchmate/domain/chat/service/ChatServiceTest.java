package com.back.catchmate.domain.chat.service;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.repository.BoardRepository;
import com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest;
import com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest.MessageType;
import com.back.catchmate.domain.chat.dto.ChatRequest.ReadChatMessageRequest;
import com.back.catchmate.domain.chat.dto.ChatResponse.PagedChatMessageInfo;
import com.back.catchmate.domain.chat.entity.ChatMessage;
import com.back.catchmate.domain.chat.entity.ChatRoom;
import com.back.catchmate.domain.chat.entity.UserChatRoom;
import com.back.catchmate.domain.chat.repository.ChatMessageRepository;
import com.back.catchmate.domain.chat.repository.ChatRoomRepository;
import com.back.catchmate.domain.chat.repository.UserChatRoomRepository;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.game.entity.Game;
import com.back.catchmate.domain.game.repository.GameRepository;
import com.back.catchmate.domain.notification.service.FCMService;
import com.back.catchmate.domain.user.entity.Authority;
import com.back.catchmate.domain.user.entity.Provider;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import jakarta.persistence.EntityManager;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ChatServiceImplTest {

    @Autowired
    private ChatService chatService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    @Autowired
    private UserChatRoomRepository userChatRoomRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private EntityManager em;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;
    @MockBean
    private FCMService fcmService;
    @MockBean
    private ChatSessionService chatSessionService;
    @MockBean
    private ChatMessageRepository chatMessageRepository;

    private User sender;
    private User receiver;
    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        Club club = clubRepository.save(createClub());
        sender = userRepository.save(createUser("sender@test.com", "Sender", club));
        receiver = userRepository.save(createUser("receiver@test.com", "Receiver", club));

        Game game = gameRepository.save(createGame(club));
        Board board = boardRepository.save(createBoard(sender, club, game));

        chatRoom = chatRoomRepository.save(ChatRoom.builder()
                .board(board)
                .participantCount(2)
                .userChatRoomList(new ArrayList<>())
                .build());

        userChatRoomRepository.save(UserChatRoom.builder()
                .user(sender)
                .chatRoom(chatRoom)
                .isNewChatRoom(false)
                .joinedAt(LocalDateTime.now())
                .isNotificationEnabled(true) // 필수
                .build());

        userChatRoomRepository.save(UserChatRoom.builder()
                .user(receiver)
                .chatRoom(chatRoom)
                .isNewChatRoom(false)
                .joinedAt(LocalDateTime.now())
                .isNotificationEnabled(true) // 필수: 그래야 알림 대상이 됨
                .build());

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("채팅 메시지 전송 성공 - 메시지 저장, WebSocket 전송, 채팅방 정보 업데이트 검증")
    void sendChatMessage_Success() throws Exception {
        // given
        ChatMessageRequest request = ChatMessageRequest.builder()
                .senderId(sender.getId())
                .content("안녕하세요")
                .messageType(MessageType.TALK)
                .build();

        // [핵심] 저장될 메시지 객체 생성 (insert 반환용)
        ChatMessage savedMessage = ChatMessage.builder()
                .id(new ObjectId())
                .chatRoomId(chatRoom.getId())
                .senderId(sender.getId())
                .content("안녕하세요")
                .sendTime(LocalDateTime.now())
                .messageType(MessageType.TALK.name())
                .build();

        // Mocking: MongoDB insert가 호출되면 위에서 만든 savedMessage를 반환하도록 설정
        given(chatMessageRepository.insert(any(ChatMessage.class))).willReturn(savedMessage);

        // Mocking: 날짜 메시지 필요 여부 (null이면 첫 메시지라 날짜 메시지 생성됨)
        given(chatMessageRepository.findFirstByChatRoomIdOrderBySendTimeDesc(chatRoom.getId())).willReturn(null);

        // Mocking: 채팅방 유저 접속 상태 (상대방은 미접속 -> 알림 전송 대상)
        given(chatSessionService.isUserInChatRoom(eq(chatRoom.getId()), eq(receiver.getId()))).willReturn(false);

        // when
        chatService.sendChatMessage(chatRoom.getId(), request);

        // then
        // 1. WebSocket 전송 확인 (날짜 메시지 + 일반 메시지 = 2회)
        verify(messagingTemplate, times(2)).convertAndSend(eq("/topic/chat." + chatRoom.getId()), any(Object.class));

        // 2. 채팅 목록 갱신 전송 확인
        verify(messagingTemplate).convertAndSend(eq("/topic/chatList"), any(Object.class));

        // 3. FCM 전송 확인 (상대방에게)
        verify(fcmService).sendMessagesByTokens(eq(chatRoom.getId()), any(), eq("안녕하세요"), eq(sender.getFcmToken()));

        // 4. 채팅방 정보(마지막 메시지 등) 업데이트 확인 (JPA)
        ChatRoom updatedChatRoom = chatRoomRepository.findById(chatRoom.getId()).orElseThrow();
        assertThat(updatedChatRoom.getLastMessageContent()).isEqualTo("안녕하세요");
    }

    @Test
    @DisplayName("채팅 읽음 처리 성공 - 마지막 읽은 시간이 업데이트되어야 한다")
    void updateLastReadTime_Success() {
        // given
        ReadChatMessageRequest request = ReadChatMessageRequest.builder()
                .userId(sender.getId())
                .chatRoomId(chatRoom.getId())
                .build();

        // when
        chatService.updateLastReadTime(request);

        // then
        UserChatRoom ucr = userChatRoomRepository.findByUserIdAndChatRoomIdAndDeletedAtIsNull(sender.getId(), chatRoom.getId()).orElseThrow();
        assertThat(ucr.getLastReadTime()).isAfter(LocalDateTime.now().minusMinutes(1));
    }

    @Test
    @DisplayName("채팅 내역 조회 성공")
    void getChatMessageList_Success() {
        // given
        ChatMessage message = ChatMessage.builder()
                .id(new ObjectId())
                .chatRoomId(chatRoom.getId())
                .content("메시지")
                .senderId(sender.getId())
                .sendTime(LocalDateTime.now())
                .messageType(MessageType.TALK.name())
                .build();

        // Mocking: MongoDB 페이징 조회
        given(chatMessageRepository.findByChatRoomIdOrderByIdDesc(any(), any()))
                .willReturn(new PageImpl<>(List.of(message)));

        // when
        PagedChatMessageInfo result = chatService.getChatMessageList(sender.getId(), chatRoom.getId(), null, 20);

        // then
        assertThat(result.getChatMessageInfoList()).hasSize(1);
        assertThat(result.getChatMessageInfoList().get(0).getContent()).isEqualTo("메시지");

        UserChatRoom ucr = userChatRoomRepository.findByUserIdAndChatRoomIdAndDeletedAtIsNull(sender.getId(), chatRoom.getId()).orElseThrow();
        assertThat(ucr.getLastReadTime()).isAfter(LocalDateTime.now().minusMinutes(1));
    }

    @Test
    @DisplayName("참여자가 아닌 채팅방의 내역 조회 시 예외 발생")
    void getChatMessageList_Fail_NotParticipant() {
        // given
        User otherUser = userRepository.save(createUser("other@test.com", "Other", clubRepository.findAll().get(0)));

        // when & then
        assertThatThrownBy(() -> chatService.getChatMessageList(otherUser.getId(), chatRoom.getId(), null, 20))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.USER_CHATROOM_NOT_FOUND.getMessage());
    }

    // --- Helper Methods ---
    private Club createClub() {
        return Club.builder().name("Test Club").region("Seoul").homeStadium("Stadium").build();
    }

    private User createUser(String email, String nickname, Club club) {
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
                .preferredAgeRange("20s")
                .liftUpDate(LocalDateTime.now())
                .build();
    }
}
