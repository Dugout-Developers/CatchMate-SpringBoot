package com.back.catchmate.domain.user.service;

import com.back.catchmate.domain.chat.service.UserChatRoomService;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.notification.service.NotificationService;
import com.back.catchmate.domain.user.dto.UserRequest.UserJoinRequest;
import com.back.catchmate.domain.user.dto.UserRequest.UserProfileUpdateRequest;
import com.back.catchmate.domain.user.dto.UserResponse.LoginInfo;
import com.back.catchmate.domain.user.dto.UserResponse.UnreadStatusInfo;
import com.back.catchmate.domain.user.dto.UserResponse.UpdateAlarmInfo;
import com.back.catchmate.domain.user.dto.UserResponse.UserInfo;
import com.back.catchmate.domain.user.entity.AlarmType;
import com.back.catchmate.domain.user.entity.Authority;
import com.back.catchmate.domain.user.entity.Provider;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.auth.repository.RefreshTokenRepository;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.error.exception.BaseException;
import com.back.catchmate.global.s3.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private ClubRepository clubRepository;

    // 외부 의존성은 Mocking
    @MockBean private S3Service s3Service;
    @MockBean private NotificationService notificationService;
    @MockBean private UserChatRoomService userChatRoomService;
    @MockBean private RefreshTokenRepository refreshTokenRepository;

    private Club club;
    private User user;

    @BeforeEach
    void setUp() {
        club = clubRepository.save(Club.builder()
                .name("KIA Tigers")
                .homeStadium("Champions Field")
                .region("Gwangju")
                .build());

        user = userRepository.save(createUser("user@test.com", "user", club));
    }

    @Test
    @DisplayName("회원가입 성공 - DB에 유저 정보가 저장되어야 한다")
    void joinUser_Success() {
        // given
        UserJoinRequest request = UserJoinRequest.builder()
                .email("new@test.com")
                .providerId("12345")
                .provider("kakao")
                .profileImageUrl("profile.jpg")
                .fcmToken("new-fcm-token")
                .gender('F')
                .nickName("NewUser")
                .birthDate(LocalDate.of(2000, 1, 1))
                .favoriteClubId(club.getId())
                .watchStyle("직관러")
                .build();

        // when
        LoginInfo loginInfo = userService.joinUser(request);

        // then
        verify(refreshTokenRepository, times(1)).save(any());
        assertThat(loginInfo.getUserId()).isNotNull();
        assertThat(loginInfo.getAccessToken()).isNotNull();
        assertThat(loginInfo.getRefreshToken()).isNotNull();

        // DB 검증
        User savedUser = userRepository.findById(loginInfo.getUserId()).orElseThrow();
        assertThat(savedUser.getEmail()).isEqualTo("new@test.com");
        assertThat(savedUser.getNickName()).isEqualTo("NewUser");
        assertThat(savedUser.getProviderId()).isEqualTo("12345@kakao"); // 생성 로직 확인
    }

    @Test
    @DisplayName("이미 존재하는 유저(ProviderId 중복)가 가입 시도 시 예외 발생")
    void joinUser_Fail_AlreadyExists() {
        // given
        // user는 이미 "google_user@test.com" (ProviderId: "google_user@test.com" 아님, createUser 메서드 확인 필요)
        // createUser 메서드: .providerId("google_" + email) -> "google_user@test.com" (X) -> "google_user@test.com"
        // AuthServiceImpl.generateProviderId 로직: providerId + "@" + provider

        // 기존 유저와 동일한 ProviderId를 생성하도록 요청 구성
        // setUp에서 저장된 user의 providerId는 "google_user@test.com" (createUser 메서드 참고)
        // 하지만 AuthServiceImpl 로직 상 "@"로 구분하므로, 테스트 데이터 정합성을 위해
        // setUp의 user를 직접 만들기보다 joinUser를 통해 만든 유저로 테스트하거나,
        // 예외 발생 조건을 정확히 맞춰야 함.

        // 여기선 새로운 유저를 먼저 가입시키고 중복 가입 시도
        UserJoinRequest request = UserJoinRequest.builder()
                .email("duplicate@test.com")
                .providerId("dupId")
                .provider("naver")
                .favoriteClubId(club.getId())
                .nickName("dupNick")
                .gender('M')
                .birthDate(LocalDate.now())
                .profileImageUrl("img")
                .fcmToken("token")
                .watchStyle("style")
                .build();
        userService.joinUser(request);

        // when & then
        assertThatThrownBy(() -> userService.joinUser(request))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("이미 가입된 사용자입니다.");
    }

    @Test
    @DisplayName("내 프로필 조회 성공")
    void getMyProfile_Success() {
        // when
        UserInfo userInfo = userService.getMyProfile(user.getId());

        // then
        assertThat(userInfo.getUserId()).isEqualTo(user.getId());
        assertThat(userInfo.getNickName()).isEqualTo(user.getNickName());
        assertThat(userInfo.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("타인 프로필 조회 성공")
    void getOtherUserProfile_Success() {
        // given
        User otherUser = userRepository.save(createUser("other@test.com", "other", club));

        // when
        UserInfo userInfo = userService.getOtherUserProfile(user.getId(), otherUser.getId());

        // then
        assertThat(userInfo.getUserId()).isEqualTo(otherUser.getId());
        assertThat(userInfo.getNickName()).isEqualTo(otherUser.getNickName());
    }

    @Test
    @DisplayName("프로필 수정 성공 - 닉네임, 스타일, 이미지가 변경되어야 한다")
    void updateProfile_Success() throws IOException {
        // given
        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
                .nickName("UpdatedNick")
                .favoriteClubId(club.getId())
                .watchStyle("응원러")
                .build();

        MockMultipartFile image = new MockMultipartFile("image", "new.jpg", "image/jpeg", "data".getBytes());
        given(s3Service.uploadFile(any())).willReturn("https://s3.url/new.jpg");

        // when
        StateResponse response = userService.updateProfile(request, image, user.getId());

        // then
        assertThat(response.isState()).isTrue();

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getNickName()).isEqualTo("UpdatedNick");
        assertThat(updatedUser.getWatchStyle()).isEqualTo("응원러");
        assertThat(updatedUser.getProfileImageUrl()).isEqualTo("https://s3.url/new.jpg");
    }

    @Test
    @DisplayName("알림 설정 변경 성공")
    void updateAlarm_Success() {
        // given
        // 초기 상태: All='Y' (createUser 참조)

        // when: 채팅 알림 끄기
        UpdateAlarmInfo info = userService.updateAlarm(user.getId(), AlarmType.CHAT, 'N');

        // then
        assertThat(info.getIsEnabled()).isEqualTo('N');

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getChatAlarm()).isEqualTo('N');
        assertThat(updatedUser.getAllAlarm()).isEqualTo('N'); // 하나라도 꺼지면 전체 알림도 N
    }

    @Test
    @DisplayName("회원 탈퇴 성공 - Soft Delete 및 FCM 토큰 삭제 확인")
    void deleteUser_Success() {
        // when
        StateResponse response = userService.deleteUser(user.getId());

        // then
        assertThat(response.isState()).isTrue();

        // DB 상태 검증
        // findByIdAndDeletedAtIsNull 조회 시 없어야 함
        assertThat(userRepository.findByIdAndDeletedAtIsNull(user.getId())).isEmpty();

        // 실제 데이터는 존재하지만 deletedAt과 fcmToken이 변경되었는지 확인 (findAll 사용)
        User deletedUser = userRepository.findAll().stream()
                .filter(u -> u.getId().equals(user.getId()))
                .findFirst().orElseThrow();

        assertThat(deletedUser.getDeletedAt()).isNotNull();
        assertThat(deletedUser.getFcmToken()).isNull();
    }

    @Test
    @DisplayName("읽지 않은 알림/채팅 여부 조회")
    void hasUnreadMessagesOrNotifications_Success() {
        // given
        given(notificationService.hasUnreadNotification(user.getId())).willReturn(true);
        given(userChatRoomService.hasUnreadChat(user.getId())).willReturn(false);

        // when
        UnreadStatusInfo info = userService.hasUnreadMessagesOrNotifications(user.getId());

        // then
        assertThat(info.isHasUnreadNotification()).isTrue();
        assertThat(info.isHasUnreadChat()).isFalse();
    }

    // --- Helper Methods ---
    private User createUser(String email, String nickname, Club club) {
        return User.builder()
                .email(email)
                .provider(Provider.GOOGLE)
                .providerId("google_" + email) // 주의: 실제 서비스 로직과 일치하는지 확인 필요
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
