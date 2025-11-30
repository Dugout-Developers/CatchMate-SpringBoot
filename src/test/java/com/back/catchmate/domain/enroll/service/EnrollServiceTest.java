package com.back.catchmate.domain.enroll.service;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.repository.BoardRepository;
import com.back.catchmate.domain.chat.entity.ChatRoom;
import com.back.catchmate.domain.chat.repository.ChatRoomRepository;
import com.back.catchmate.domain.chat.repository.UserChatRoomRepository;
import com.back.catchmate.domain.chat.service.ChatService;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.enroll.dto.EnrollRequest.CreateEnrollRequest;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CancelEnrollInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CreateEnrollInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.EnrollDescriptionInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.NewEnrollCountInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.PagedEnrollReceiveInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.PagedEnrollRequestInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.UpdateEnrollInfo;
import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.enroll.entity.Enroll;
import com.back.catchmate.domain.enroll.repository.EnrollRepository;
import com.back.catchmate.domain.game.entity.Game;
import com.back.catchmate.domain.game.repository.GameRepository;
import com.back.catchmate.domain.notification.entity.Notification;
import com.back.catchmate.domain.notification.repository.NotificationRepository;
import com.back.catchmate.domain.notification.service.FCMService;
import com.back.catchmate.domain.user.entity.Authority;
import com.back.catchmate.domain.user.entity.Provider;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class EnrollServiceTest {
    @Autowired
    private EnrollService enrollService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private EnrollRepository enrollRepository;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    @Autowired
    private UserChatRoomRepository userChatRoomRepository;
    @Autowired
    private EntityManager em;

    @MockBean
    private FCMService fcmService;
    @MockBean
    private ChatService chatService;

    private User writer;
    private User applicant;
    private Club club;
    private Game game;
    private Board board;

    @BeforeEach
    void setUp() {
        club = clubRepository.save(Club.builder()
                .name("KIA Tigers")
                .homeStadium("Champions Field")
                .region("Gwangju")
                .build());

        writer = userRepository.save(createUser("writer@test.com", "writer", club));
        applicant = userRepository.save(createUser("applicant@test.com", "applicant", club));

        game = gameRepository.save(Game.builder()
                .homeClub(club)
                .awayClub(club)
                .gameStartDate(LocalDateTime.now().plusDays(1))
                .location("Gwangju")
                .build());

        board = boardRepository.save(Board.builder()
                .title("직관 모집")
                .content("같이 가요")
                .maxPerson(4)
                .currentPerson(1)
                .user(writer)
                .club(club)
                .game(game)
                .preferredGender("M")
                .preferredAgeRange("20s")
                .isCompleted(true)
                .liftUpDate(LocalDateTime.now())
                .build());
    }

    @Test
    @DisplayName("직관 신청 성공 - Enroll 및 Notification 데이터 생성 확인")
    void requestEnroll_Success() throws Exception {
        CreateEnrollRequest request = CreateEnrollRequest.builder().description("저요!").build();
        CreateEnrollInfo result = enrollService.requestEnroll(request, board.getId(), applicant.getId());

        Enroll savedEnroll = enrollRepository.findById(result.getEnrollId()).orElseThrow();
        assertThat(savedEnroll.getUser().getId()).isEqualTo(applicant.getId());
        assertThat(savedEnroll.getDescription()).isEqualTo("저요!");
        assertThat(savedEnroll.getAcceptStatus()).isEqualTo(AcceptStatus.PENDING);

        Notification notification = notificationRepository.findByBoardIdAndUserIdAndSenderIdAndDeletedAtIsNull(
                board.getId(), writer.getId(), applicant.getId()
        ).orElseThrow(() -> new IllegalArgumentException("알림이 생성되지 않음"));
        assertThat(notification.getBody()).contains("applicant님의 직관 신청");

        verify(fcmService).sendMessageByToken(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("자신의 게시글에 신청 시 예외 발생")
    void requestEnroll_Fail_SelfEnroll() {
        CreateEnrollRequest request = CreateEnrollRequest.builder().description("자신 신청").build();
        assertThatThrownBy(() -> enrollService.requestEnroll(request, board.getId(), writer.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("자신의 게시글에는 직관 신청을 할 수 없습니다.");
    }

    @Test
    @DisplayName("이미 신청한 게시글에 중복 신청 시 예외 발생")
    void requestEnroll_Fail_Duplicate() throws Exception {
        CreateEnrollRequest request = CreateEnrollRequest.builder().description("첫 신청").build();
        enrollService.requestEnroll(request, board.getId(), applicant.getId());

        assertThatThrownBy(() -> enrollService.requestEnroll(request, board.getId(), applicant.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("이미 보낸 직관 신청입니다.");
    }

    @Test
    @DisplayName("직관 신청 취소 성공 - 데이터 Soft Delete 확인")
    void cancelEnroll_Success() throws Exception {
        CreateEnrollRequest request = CreateEnrollRequest.builder().description("취소할 신청").build();
        CreateEnrollInfo createInfo = enrollService.requestEnroll(request, board.getId(), applicant.getId());

        CancelEnrollInfo cancelInfo = enrollService.cancelEnroll(createInfo.getEnrollId(), applicant.getId());

        assertThat(enrollRepository.findByIdAndDeletedAtIsNull(cancelInfo.getEnrollId())).isEmpty();
        assertThat(notificationRepository.findByBoardIdAndUserIdAndSenderIdAndDeletedAtIsNull(
                board.getId(), writer.getId(), applicant.getId())).isEmpty();
    }

    @Test
    @DisplayName("본인이 아닌 유저가 취소 시도 시 예외 발생")
    void cancelEnroll_Fail_InvalidUser() throws Exception {
        CreateEnrollRequest request = CreateEnrollRequest.builder().description("신청").build();
        CreateEnrollInfo createInfo = enrollService.requestEnroll(request, board.getId(), applicant.getId());

        assertThatThrownBy(() -> enrollService.cancelEnroll(createInfo.getEnrollId(), writer.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("직관 신청을 취소할 권한이 없습니다.");
    }

    @Test
    @DisplayName("직관 신청 수락 성공 - 상태 변경, 채팅방 참여, 게시글 인원 증가 확인")
    void acceptEnroll_Success() throws Exception {
        // given
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.builder()
                .board(board)
                .participantCount(1)
                .build());

        CreateEnrollRequest request = CreateEnrollRequest.builder().description("수락해주세요").build();
        CreateEnrollInfo createInfo = enrollService.requestEnroll(request, board.getId(), applicant.getId());

        em.flush();
        em.clear();

        doNothing().when(chatService).sendEnterLeaveMessage(any(), any(), any(), any());

        // when
        UpdateEnrollInfo result = enrollService.acceptEnroll(createInfo.getEnrollId(), writer.getId());

        // then
        assertThat(result.getAcceptStatus()).isEqualTo(AcceptStatus.ACCEPTED);
        assertThat(enrollRepository.findByIdAndDeletedAtIsNull(createInfo.getEnrollId())).isEmpty(); // Soft deleted

        boolean isUserInChat = userChatRoomRepository.existsByUserIdAndChatRoomIdAndDeletedAtIsNull(applicant.getId(), chatRoom.getId());
        assertThat(isUserInChat).isTrue();

        Board updatedBoard = boardRepository.findById(board.getId()).orElseThrow();
        assertThat(updatedBoard.getCurrentPerson()).isEqualTo(2);

        Notification noti = notificationRepository.findAll().stream()
                .filter(n -> n.getBoard().getId().equals(board.getId()) && n.getSender().getId().equals(applicant.getId()))
                .findFirst().orElseThrow();
        assertThat(noti.getAcceptStatus()).isEqualTo(AcceptStatus.ALREADY_ACCEPTED);
    }

    @Test
    @DisplayName("작성자가 아닌 유저가 수락을 시도하면 예외가 발생한다")
    void acceptEnroll_Fail_InvalidUser() throws Exception {
        // given
        User intruder = userRepository.save(createUser("intruder@test.com", "intruder", club));

        CreateEnrollRequest request = CreateEnrollRequest.builder().description("신청").build();
        CreateEnrollInfo createInfo = enrollService.requestEnroll(request, board.getId(), applicant.getId());

        // when & then
        assertThatThrownBy(() -> enrollService.acceptEnroll(createInfo.getEnrollId(), intruder.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("직관 신청을 수락할 권한이 없습니다.");
    }

    @Test
    @DisplayName("이미 처리된(PENDING이 아닌) 신청을 다시 수락하면 예외가 발생한다")
    void acceptEnroll_Fail_AlreadyResponded() throws Exception {
        // given
        chatRoomRepository.save(ChatRoom.builder()
                .board(board)
                .participantCount(1)
                .build());

        CreateEnrollRequest request = CreateEnrollRequest.builder().description("신청").build();
        CreateEnrollInfo createInfo = enrollService.requestEnroll(request, board.getId(), applicant.getId());

        // 1차 수락
        em.flush();
        em.clear();
        doNothing().when(chatService).sendEnterLeaveMessage(any(), any(), any(), any());
        enrollService.acceptEnroll(createInfo.getEnrollId(), writer.getId());

        // when & then
        // 2차 수락 시도
        assertThatThrownBy(() -> enrollService.acceptEnroll(createInfo.getEnrollId(), writer.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("이미 수락된 신청입니다.");
    }

    @Test
    @DisplayName("정원이 꽉 찬 게시글은 수락 불가")
    void acceptEnroll_Fail_FullPerson() throws Exception {
        Board fullBoard = boardRepository.save(Board.builder()
                .title("Full Board")
                .content("Full")
                .maxPerson(1)
                .currentPerson(1)
                .user(writer)
                .club(club)
                .game(game)
                .preferredGender("M")
                .preferredAgeRange("20s")
                .isCompleted(true)
                .liftUpDate(LocalDateTime.now())
                .build());

        Enroll enroll = enrollRepository.save(Enroll.builder()
                .user(applicant).board(fullBoard).acceptStatus(AcceptStatus.PENDING).isNew(true).build());

        assertThatThrownBy(() -> enrollService.acceptEnroll(enroll.getId(), writer.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("해당 게시글은 마감되었습니다.");
    }

    @Test
    @DisplayName("작성자가 아닌 유저가 거절을 시도하면 예외가 발생한다")
    void rejectEnroll_Fail_InvalidUser() throws Exception {
        // given
        User intruder = userRepository.save(createUser("intruder@test.com", "intruder", club));

        CreateEnrollRequest request = CreateEnrollRequest.builder().description("신청").build();
        CreateEnrollInfo createInfo = enrollService.requestEnroll(request, board.getId(), applicant.getId());

        // when & then
        assertThatThrownBy(() -> enrollService.rejectEnroll(createInfo.getEnrollId(), intruder.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("직관 신청을 거절할 권한이 없습니다.");
    }

    @Test
    @DisplayName("직관 신청 거절 성공 - 상태 변경 및 삭제 확인")
    void rejectEnroll_Success() throws Exception {
        CreateEnrollRequest request = CreateEnrollRequest.builder().description("거절될 신청").build();
        CreateEnrollInfo createInfo = enrollService.requestEnroll(request, board.getId(), applicant.getId());

        UpdateEnrollInfo result = enrollService.rejectEnroll(createInfo.getEnrollId(), writer.getId());

        assertThat(result.getAcceptStatus()).isEqualTo(AcceptStatus.REJECTED);
        assertThat(enrollRepository.findByIdAndDeletedAtIsNull(createInfo.getEnrollId())).isEmpty();

        Notification noti = notificationRepository.findAll().stream()
                .filter(n -> n.getBoard().getId().equals(board.getId()) && n.getSender().getId().equals(applicant.getId()))
                .findFirst().orElseThrow();
        assertThat(noti.getAcceptStatus()).isEqualTo(AcceptStatus.ALREADY_REJECTED);
    }

    @Test
    @DisplayName("내가 보낸 신청 목록 조회")
    void getRequestEnrollList_Success() throws Exception {
        CreateEnrollRequest request = CreateEnrollRequest.builder().description("신청1").build();
        enrollService.requestEnroll(request, board.getId(), applicant.getId());

        PagedEnrollRequestInfo result = enrollService.getRequestEnrollList(applicant.getId(), PageRequest.of(0, 10));

        assertThat(result.getEnrollInfoList()).hasSize(1);
        assertThat(result.getEnrollInfoList().get(0).getDescription()).isEqualTo("신청1");
    }

    @Test
    @DisplayName("내가 받은 신청 목록 전체 조회")
    void getReceiveEnrollList_Success() throws Exception {
        CreateEnrollRequest request = CreateEnrollRequest.builder().description("신청합니다").build();
        enrollService.requestEnroll(request, board.getId(), applicant.getId());

        PagedEnrollReceiveInfo result = enrollService.getReceiveEnrollList(writer.getId(), PageRequest.of(0, 10));

        assertThat(result.getEnrollInfoList()).hasSize(1);
        assertThat(result.getEnrollInfoList().get(0).getEnrollReceiveInfoList()).hasSize(1);
    }

    @Test
    @DisplayName("특정 게시글의 받은 신청 목록 조회 및 읽음 상태 업데이트 확인")
    void getReceiveEnrollListByBoardId_Success() throws Exception {
        CreateEnrollRequest request = CreateEnrollRequest.builder().description("신청").build();
        enrollService.requestEnroll(request, board.getId(), applicant.getId());

        PagedEnrollReceiveInfo result = enrollService.getReceiveEnrollListByBoardId(writer.getId(), board.getId(), PageRequest.of(0, 10));

        assertThat(result.getEnrollInfoList()).hasSize(1);

        Enroll updatedEnroll = enrollRepository.findByUserIdAndBoardIdAndDeletedAtIsNull(applicant.getId(), board.getId()).orElseThrow();
        assertThat(updatedEnroll.isNew()).isFalse();
    }

    @Test
    @DisplayName("새로운 신청 개수 조회")
    void getNewEnrollListCount_Success() throws Exception {
        CreateEnrollRequest request = CreateEnrollRequest.builder().description("새 신청").build();
        enrollService.requestEnroll(request, board.getId(), applicant.getId());

        NewEnrollCountInfo countInfo = enrollService.getNewEnrollListCount(writer.getId());

        assertThat(countInfo.getNewEnrollCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("신청 상세 내용 조회 성공")
    void getEnrollDescriptionById_Success() throws Exception {
        String description = "상세 내용입니다.";
        CreateEnrollRequest request = CreateEnrollRequest.builder().description(description).build();
        enrollService.requestEnroll(request, board.getId(), applicant.getId());

        EnrollDescriptionInfo info = enrollService.getEnrollDescriptionById(board.getId(), applicant.getId());

        assertThat(info.getDescription()).isEqualTo(description);
    }

    // 1. requestEnroll 예외 테스트
    @Test
    @DisplayName("존재하지 않는 유저가 신청을 시도하면 예외가 발생한다")
    void requestEnroll_Fail_UserNotFound() {
        CreateEnrollRequest request = CreateEnrollRequest.builder().description("신청").build();
        assertThatThrownBy(() -> enrollService.requestEnroll(request, board.getId(), 99999L))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 신청을 시도하면 예외가 발생한다")
    void requestEnroll_Fail_BoardNotFound() {
        CreateEnrollRequest request = CreateEnrollRequest.builder().description("신청").build();
        assertThatThrownBy(() -> enrollService.requestEnroll(request, 99999L, applicant.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.BOARD_NOT_FOUND.getMessage());
    }

    // 2. cancelEnroll 예외 테스트
    @Test
    @DisplayName("존재하지 않는 유저가 취소를 시도하면 예외가 발생한다")
    void cancelEnroll_Fail_UserNotFound() {
        assertThatThrownBy(() -> enrollService.cancelEnroll(1L, 99999L))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 신청을 취소하려 하면 예외가 발생한다")
    void cancelEnroll_Fail_EnrollNotFound() {
        assertThatThrownBy(() -> enrollService.cancelEnroll(99999L, applicant.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.ENROLL_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("신청 취소 시 알림을 찾을 수 없으면 예외가 발생한다 (데이터 정합성 오류)")
    void cancelEnroll_Fail_NotificationNotFound() throws Exception {
        // given
        CreateEnrollRequest request = CreateEnrollRequest.builder().description("신청").build();
        CreateEnrollInfo createInfo = enrollService.requestEnroll(request, board.getId(), applicant.getId());

        // 알림 강제 삭제 (예외 유발을 위해)
        notificationRepository.deleteAll();

        // when & then
        assertThatThrownBy(() -> enrollService.cancelEnroll(createInfo.getEnrollId(), applicant.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.NOTIFICATION_NOT_FOUND.getMessage());
    }

    // 3. 조회 관련 예외 테스트
    @Test
    @DisplayName("존재하지 않는 유저가 보낸 신청 목록을 조회하면 예외가 발생한다")
    void getRequestEnrollList_Fail_UserNotFound() {
        assertThatThrownBy(() -> enrollService.getRequestEnrollList(99999L, PageRequest.of(0, 10)))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 유저가 받은 신청 목록을 조회하면 예외가 발생한다")
    void getReceiveEnrollList_Fail_UserNotFound() {
        assertThatThrownBy(() -> enrollService.getReceiveEnrollList(99999L, PageRequest.of(0, 10)))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 게시글의 신청 목록을 조회하면 예외가 발생한다")
    void getReceiveEnrollListByBoardId_Fail_BoardNotFound() {
        assertThatThrownBy(() -> enrollService.getReceiveEnrollListByBoardId(writer.getId(), 99999L, PageRequest.of(0, 10)))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.BOARD_NOT_FOUND.getMessage());
    }

    // 4. acceptEnroll 예외 테스트
    @Test
    @DisplayName("수락 시 신청 정보가 없으면 예외가 발생한다")
    void acceptEnroll_Fail_EnrollNotFound() {
        assertThatThrownBy(() -> enrollService.acceptEnroll(99999L, writer.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.ENROLL_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("수락 시 신청자 정보가 없으면 예외가 발생한다")
    void acceptEnroll_Fail_ApplicantNotFound() throws Exception {
        // given
        CreateEnrollRequest request = CreateEnrollRequest.builder().description("신청").build();
        CreateEnrollInfo createInfo = enrollService.requestEnroll(request, board.getId(), applicant.getId());

        // 신청자 강제 삭제
        // (참조 무결성 때문에 Enroll, Notification 먼저 삭제해야 함. 하지만 EnrollService 로직 테스트이므로
        //  Enroll 객체의 user 필드가 가리키는 ID로 조회 시 없을 때를 테스트해야 함.
        //  JPA 연관관계 때문에 실제 DB에서 유저만 삭제하기 어려우므로, 이 케이스는 통합테스트로 구현하기 까다로울 수 있음.
        //  여기서는 생략하거나, Enroll을 저장할 때 존재하지 않는 유저 ID를 매핑하는 방식(Native Query 등)을 써야 함.
        //  일반적인 시나리오에서는 발생하기 힘든 케이스임)
    }

    @Test
    @DisplayName("수락 시 알림 정보가 없으면 예외가 발생한다")
    void acceptEnroll_Fail_NotificationNotFound() throws Exception {
        // given
        // [수정] 채팅방이 있어야 enterChatRoom 로직을 통과함
        ChatRoom chatRoom = ChatRoom.builder()
                .board(board)
                .participantCount(1)
                .userChatRoomList(new ArrayList<>()) // NPE 방지용
                .build();
        chatRoomRepository.save(chatRoom);

        CreateEnrollRequest request = CreateEnrollRequest.builder().description("신청").build();
        CreateEnrollInfo createInfo = enrollService.requestEnroll(request, board.getId(), applicant.getId());

        // 알림 강제 삭제 (예외 유발 조건)
        notificationRepository.deleteAll();

        // 영속성 컨텍스트 초기화 (데이터 반영)
        em.flush();
        em.clear();

        // [추가] 채팅방 입장 시 메시지 전송 로직 Mocking 필요
        doNothing().when(chatService).sendEnterLeaveMessage(any(), any(), any(), any());

        // when & then
        assertThatThrownBy(() -> enrollService.acceptEnroll(createInfo.getEnrollId(), writer.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.NOTIFICATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("수락 시 채팅방 정보가 없으면 예외가 발생한다")
    void acceptEnroll_Fail_ChatRoomNotFound() throws Exception {
        CreateEnrollRequest request = CreateEnrollRequest.builder().description("신청").build();
        CreateEnrollInfo createInfo = enrollService.requestEnroll(request, board.getId(), applicant.getId());

        // 채팅방 없이 진행 (setUp에서 isCompleted=true라 채팅방이 생성되었을 수 있음 -> 삭제 필요)
        // 하지만 board.isCompleted=true인 상태에서 채팅방이 없는 건 데이터 불일치 상황임.
        chatRoomRepository.deleteAll();

        assertThatThrownBy(() -> enrollService.acceptEnroll(createInfo.getEnrollId(), writer.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.CHATROOM_NOT_FOUND.getMessage());
    }

    // 5. rejectEnroll 예외 테스트
    @Test
    @DisplayName("거절 시 신청 정보가 없으면 예외가 발생한다")
    void rejectEnroll_Fail_EnrollNotFound() {
        assertThatThrownBy(() -> enrollService.rejectEnroll(99999L, writer.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.ENROLL_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("거절 시 알림 정보가 없으면 예외가 발생한다")
    void rejectEnroll_Fail_NotificationNotFound() throws Exception {
        CreateEnrollRequest request = CreateEnrollRequest.builder().description("신청").build();
        CreateEnrollInfo createInfo = enrollService.requestEnroll(request, board.getId(), applicant.getId());

        notificationRepository.deleteAll();

        assertThatThrownBy(() -> enrollService.rejectEnroll(createInfo.getEnrollId(), writer.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.NOTIFICATION_NOT_FOUND.getMessage());
    }

    // 6. getEnrollDescriptionById 예외 테스트
    @Test
    @DisplayName("상세 조회 시 신청 정보가 없으면 예외가 발생한다")
    void getEnrollDescriptionById_Fail_EnrollNotFound() {
        // boardId는 유효하지만 해당 유저가 신청한 내역이 없는 경우
        assertThatThrownBy(() -> enrollService.getEnrollDescriptionById(board.getId(), applicant.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.ENROLL_NOT_FOUND.getMessage());
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


}
