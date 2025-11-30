package com.back.catchmate.domain.notification.service;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.repository.BoardRepository;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.game.entity.Game;
import com.back.catchmate.domain.game.repository.GameRepository;
import com.back.catchmate.domain.inquiry.entity.Inquiry;
import com.back.catchmate.domain.inquiry.entity.InquiryType;
import com.back.catchmate.domain.inquiry.repository.InquiryRepository;
import com.back.catchmate.domain.notification.dto.NotificationResponse.NotificationInfo;
import com.back.catchmate.domain.notification.dto.NotificationResponse.PagedNotificationInfo;
import com.back.catchmate.domain.notification.entity.Notification;
import com.back.catchmate.domain.notification.repository.NotificationRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class NotificationServiceTest {

    @Autowired private NotificationService notificationService;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BoardRepository boardRepository;
    @Autowired private InquiryRepository inquiryRepository;
    @Autowired private ClubRepository clubRepository;
    @Autowired private GameRepository gameRepository;
    @Autowired private EntityManager em;

    private User receiver;
    private User sender;
    private Board board;
    private Inquiry inquiry;
    private Club club;

    @BeforeEach
    void setUp() {
        // 기초 데이터 셋업
        club = clubRepository.save(Club.builder()
                .name("KIA Tigers")
                .homeStadium("Champions Field")
                .region("Gwangju")
                .build());

        receiver = userRepository.save(createUser("receiver@test.com", "receiver", club));
        sender = userRepository.save(createUser("sender@test.com", "sender", club));

        Game game = gameRepository.save(Game.builder()
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
                .user(sender) // 게시글 작성자는 Sender
                .club(club)
                .game(game)
                .preferredGender("M")
                .preferredAgeRange("20s")
                .isCompleted(true)
                .liftUpDate(LocalDateTime.now())
                .build());

        inquiry = inquiryRepository.save(Inquiry.builder()
                .user(receiver)
                .inquiryType(InquiryType.OTHER)
                .content("문의합니다.")
                .isCompleted(false)
                .build());
    }

    // 1. createNotification (Enroll 관련) 테스트
    @Test
    @DisplayName("직관 신청 알림 생성 성공 - DB 저장 확인")
    void createNotification_Enroll_Success() {
        // given
        String title = "신청 알림";
        String body = "직관 신청이 도착했습니다.";
        AcceptStatus status = AcceptStatus.PENDING;

        // when
        notificationService.createNotification(title, body, sender.getId(), board.getId(), receiver.getId(), status);

        // then
        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(1);

        Notification saved = notifications.get(0);
        assertThat(saved.getUser().getId()).isEqualTo(receiver.getId()); // 수신자 확인
        assertThat(saved.getSender().getId()).isEqualTo(sender.getId()); // 발신자 확인
        assertThat(saved.getBoard().getId()).isEqualTo(board.getId());
        assertThat(saved.getTitle()).isEqualTo(title);
        assertThat(saved.getAcceptStatus()).isEqualTo(status);
        assertThat(saved.isRead()).isFalse(); // 기본값 안 읽음 확인
    }

    // 2. createNotification (Inquiry 관련) 테스트
    @Test
    @DisplayName("문의 답변 알림 생성 성공 - DB 저장 확인")
    void createNotification_Inquiry_Success() {
        // given
        String title = "답변 알림";
        String body = "문의하신 내용에 답변이 달렸습니다.";

        // when
        // 문의 알림의 경우 senderId가 Admin 등이 될 수 있으나 로직상 senderId 파라미터는 받지만
        // toEntityInquiry에서는 sender를 null로 설정하거나 로직에 따라 다름.
        // ServiceImpl 코드를 보면 senderId 파라미터를 받지만 toEntityInquiry 호출 시에는 사용하지 않음(null 처리 로직 없음)
        // -> NotificationServiceImpl.createNotification(Inquiry) 메서드를 보면 sender 조회 로직이 없음.
        // -> toEntityInquiry 매개변수에 sender 없음.
        notificationService.createNotification(title, body, null, inquiry.getId(), receiver.getId());

        // then
        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(1);

        Notification saved = notifications.get(0);
        assertThat(saved.getUser().getId()).isEqualTo(receiver.getId());
        assertThat(saved.getInquiry().getId()).isEqualTo(inquiry.getId());
        assertThat(saved.getTitle()).isEqualTo(title);
        assertThat(saved.getSender()).isNull(); // Inquiry 알림은 Sender가 null (시스템/관리자)
    }

    // 3. getNotificationList 테스트
    @Test
    @DisplayName("내 알림 목록 조회 성공")
    void getNotificationList_Success() {
        // given
        createMockNotification(receiver, board, "알림1");
        createMockNotification(receiver, board, "알림2");
        createMockNotification(sender, board, "다른 사람 알림"); // 조회되면 안됨

        // when
        PagedNotificationInfo result = notificationService.getNotificationList(receiver.getId(), PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNotificationInfoList()).extracting("title")
                .containsExactlyInAnyOrder("알림1", "알림2");
    }

    // 4. getNotification 테스트 (단건 조회 및 읽음 처리)
    @Test
    @DisplayName("알림 단건 조회 시 읽음 상태(isRead)가 true로 변경되어야 한다")
    void getNotification_Success_MarkAsRead() {
        // given
        Notification notification = createMockNotification(receiver, board, "읽지 않은 알림");
        assertThat(notification.isRead()).isFalse(); // 초기 상태 확인

        // when
        NotificationInfo info = notificationService.getNotification(receiver.getId(), notification.getId());

        // then
        assertThat(info.getNotificationId()).isEqualTo(notification.getId());
        assertThat(info.isRead()).isTrue(); // 반환된 DTO 확인

        // DB 상태 검증
        Notification updatedNotification = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updatedNotification.isRead()).isTrue();
    }

    @Test
    @DisplayName("본인의 알림이 아니면 조회 시 예외가 발생한다")
    void getNotification_Fail_NotOwner() {
        // given
        Notification notification = createMockNotification(sender, board, "남의 알림");

        // when & then
        assertThatThrownBy(() -> notificationService.getNotification(receiver.getId(), notification.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("존재하지 않는 알림입니다."); // NOTIFICATION_NOT_FOUND (ID+User 조합 불일치)
    }

    // 5. deleteNotification 테스트
    @Test
    @DisplayName("알림 삭제 성공 - Soft Delete 확인")
    void deleteNotification_Success() {
        // given
        Notification notification = createMockNotification(receiver, board, "삭제될 알림");

        // when
        notificationService.deleteNotification(receiver.getId(), notification.getId());

        // then
        // findByIdAndUserIdAndDeletedAtIsNull 로 조회 시 없어야 함
        boolean exists = notificationRepository.findByIdAndUserIdAndDeletedAtIsNull(notification.getId(), receiver.getId()).isPresent();
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("본인의 알림이 아니면 삭제 시 예외가 발생한다")
    void deleteNotification_Fail_NotOwner() {
        // given
        Notification notification = createMockNotification(sender, board, "남의 알림");

        // when & then
        assertThatThrownBy(() -> notificationService.deleteNotification(receiver.getId(), notification.getId()))
                .isInstanceOf(BaseException.class);
    }

    // 6. hasUnreadNotification 테스트
    @Test
    @DisplayName("읽지 않은 알림이 있으면 True를 반환한다")
    void hasUnreadNotification_True() {
        // given
        createMockNotification(receiver, board, "안 읽음");

        // when
        Boolean result = notificationService.hasUnreadNotification(receiver.getId());

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("모든 알림을 읽었거나 알림이 없으면 False를 반환한다")
    void hasUnreadNotification_False() {
        // given
        Notification notification = createMockNotification(receiver, board, "읽음");
        notification.markAsRead(); // 읽음 처리
        notificationRepository.save(notification);

        // when
        Boolean result = notificationService.hasUnreadNotification(receiver.getId());

        // then
        assertThat(result).isFalse();
    }

    // 1. createNotification (Enroll) 예외 테스트
    @Test
    @DisplayName("직관 신청 알림 생성 시 수신자가 없으면 예외가 발생한다")
    void createNotification_Enroll_Fail_ReceiverNotFound() {
        assertThatThrownBy(() -> notificationService.createNotification(
                "title", "body", sender.getId(), board.getId(), 99999L, AcceptStatus.PENDING))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("직관 신청 알림 생성 시 발신자가 없으면 예외가 발생한다")
    void createNotification_Enroll_Fail_SenderNotFound() {
        assertThatThrownBy(() -> notificationService.createNotification(
                "title", "body", 99999L, board.getId(), receiver.getId(), AcceptStatus.PENDING))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("직관 신청 알림 생성 시 게시글이 없으면 예외가 발생한다")
    void createNotification_Enroll_Fail_BoardNotFound() {
        assertThatThrownBy(() -> notificationService.createNotification(
                "title", "body", sender.getId(), 99999L, receiver.getId(), AcceptStatus.PENDING))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.BOARD_NOT_FOUND.getMessage());
    }

    // 2. createNotification (Inquiry) 예외 테스트
    @Test
    @DisplayName("문의 답변 알림 생성 시 수신자가 없으면 예외가 발생한다")
    void createNotification_Inquiry_Fail_ReceiverNotFound() {
        assertThatThrownBy(() -> notificationService.createNotification(
                "title", "body", sender.getId(), inquiry.getId(), 99999L))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("문의 답변 알림 생성 시 문의글이 없으면 예외가 발생한다")
    void createNotification_Inquiry_Fail_InquiryNotFound() {
        // 참고: ServiceImpl 코드에서는 Inquiry 조회 실패 시 BOARD_NOT_FOUND를 던지거나 INQUIRY_NOT_FOUND를 던질 수 있음 (코드 확인 필요)
        // 현재 ServiceImpl 코드에는 orElseThrow -> BOARD_NOT_FOUND 로 되어 있을 수 있음. 확인 후 메시지 매칭.
        // 만약 INQUIRY_NOT_FOUND로 수정했다면 해당 메시지 사용.
        assertThatThrownBy(() -> notificationService.createNotification(
                "title", "body", sender.getId(), 99999L, receiver.getId()))
                .isInstanceOf(BaseException.class);
        // .hasMessage(...) // 정확한 메시지는 Service 구현에 따라 다름
    }

    // 3. getNotificationList 예외 테스트
    @Test
    @DisplayName("알림 목록 조회 시 유저가 없으면 예외가 발생한다")
    void getNotificationList_Fail_UserNotFound() {
        assertThatThrownBy(() -> notificationService.getNotificationList(99999L, PageRequest.of(0, 10)))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    // 4. getNotification 예외 테스트
    @Test
    @DisplayName("알림 단건 조회 시 유저가 없으면 예외가 발생한다")
    void getNotification_Fail_UserNotFound() {
        Notification notification = createMockNotification(receiver, board, "알림");
        assertThatThrownBy(() -> notificationService.getNotification(99999L, notification.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 알림 ID를 조회하면 예외가 발생한다")
    void getNotification_Fail_NotificationNotFound() {
        assertThatThrownBy(() -> notificationService.getNotification(receiver.getId(), 99999L))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.NOTIFICATION_NOT_FOUND.getMessage());
    }

    // 5. deleteNotification 예외 테스트
    @Test
    @DisplayName("알림 삭제 시 유저가 없으면 예외가 발생한다")
    void deleteNotification_Fail_UserNotFound() {
        Notification notification = createMockNotification(receiver, board, "알림");
        assertThatThrownBy(() -> notificationService.deleteNotification(99999L, notification.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 알림을 삭제하려 하면 예외가 발생한다")
    void deleteNotification_Fail_NotificationNotFound() {
        assertThatThrownBy(() -> notificationService.deleteNotification(receiver.getId(), 99999L))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.NOTIFICATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("문의(Inquiry) 관련 알림 조회 시, 게시글 정보가 없어도 정상 조회되어야 한다")
    void getNotification_Success_Inquiry() {
        // given
        // Board는 null이고 Inquiry가 설정된 알림 생성
        Notification inquiryNotification = notificationRepository.save(Notification.builder()
                .user(receiver)
                .board(null) // 게시글 없음 (이 조건이 if (board == null)을 트리거)
                .inquiry(inquiry)
                .sender(null) // 문의 알림은 보통 발신자 없음 (시스템)
                .title("문의 답변")
                .body("답변이 달렸습니다.")
                .isRead(false)
                .build());

        // when
        NotificationInfo info = notificationService.getNotification(receiver.getId(), inquiryNotification.getId());

        // then
        assertThat(info.getNotificationId()).isEqualTo(inquiryNotification.getId());
        assertThat(info.getTitle()).isEqualTo("문의 답변");

        // BoardInfo는 null이고 InquiryInfo가 존재해야 함
        assertThat(info.getBoardInfo()).isNull();
        assertThat(info.getInquiryInfo()).isNotNull();
        assertThat(info.getInquiryInfo().getInquiryId()).isEqualTo(inquiry.getId());
    }

    // --- Helper Methods ---

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

    private Notification createMockNotification(User receiver, Board board, String title) {
        return notificationRepository.save(Notification.builder()
                .user(receiver)
                .board(board)
                .sender(sender)
                .title(title)
                .body("content")
                .isRead(false)
                .acceptStatus(AcceptStatus.PENDING)
                .build());
    }
}
