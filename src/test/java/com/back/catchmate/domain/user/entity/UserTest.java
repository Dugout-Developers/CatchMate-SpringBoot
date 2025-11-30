package com.back.catchmate.domain.user.entity;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.user.dto.UserRequest;
import com.back.catchmate.global.error.exception.BaseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserTest {

    @Test
    @DisplayName("프로필 업데이트 테스트 - 닉네임, 응원스타일, 구단 변경 확인")
    void updateProfile() {
        // given
        User user = createUser();
        Club newClub = Club.builder().id(2L).name("New Club").build();
        UserRequest.UserProfileUpdateRequest request = UserRequest.UserProfileUpdateRequest.builder()
                .nickName("newNick")
                .watchStyle("직관러")
                .build();

        // when
        user.updateProfile(request, newClub);

        // then
        assertThat(user.getNickName()).isEqualTo("newNick");
        assertThat(user.getWatchStyle()).isEqualTo("직관러");
        assertThat(user.getClub()).isEqualTo(newClub);
    }

    @Test
    @DisplayName("프로필 이미지 업데이트 테스트")
    void updateProfileImageUrl() {
        // given
        User user = createUser();
        String newImageUrl = "http://new-image.url";

        // when
        user.updateProfileImageUrl(newImageUrl);

        // then
        assertThat(user.getProfileImageUrl()).isEqualTo(newImageUrl);
    }

    @Test
    @DisplayName("알림 설정 업데이트 - 전체 알림(ALL) 설정 시 모든 알림 동기화 확인")
    void updateAlarmSetting_All() {
        // given
        User user = User.builder()
                .allAlarm('N').chatAlarm('N').enrollAlarm('N').eventAlarm('N')
                .build();

        // when
        user.updateAlarmSetting(AlarmType.ALL, 'Y');

        // then
        assertThat(user.getAllAlarm()).isEqualTo('Y');
        assertThat(user.getChatAlarm()).isEqualTo('Y');
        assertThat(user.getEnrollAlarm()).isEqualTo('Y');
        assertThat(user.getEventAlarm()).isEqualTo('Y');
    }

    @Test
    @DisplayName("알림 설정 업데이트 - 개별 알림 변경 시 전체 알림 상태 동기화 (모두 켜짐 -> ALL 켜짐)")
    void updateAlarmSetting_Sync_EnableAll() {
        // given
        // 채팅, 이벤트는 켜져있고, 신청 알림만 꺼져있는 상태
        User user = User.builder()
                .allAlarm('N').chatAlarm('Y').enrollAlarm('N').eventAlarm('Y')
                .build();

        // when
        user.updateAlarmSetting(AlarmType.ENROLL, 'Y');

        // then
        // 모든 개별 알림이 Y가 되었으므로, ALL 알림도 자동으로 Y가 되어야 함
        assertThat(user.getEnrollAlarm()).isEqualTo('Y');
        assertThat(user.getAllAlarm()).isEqualTo('Y');
    }

    @Test
    @DisplayName("알림 설정 업데이트 - 개별 알림 변경 시 전체 알림 상태 동기화 (하나라도 꺼짐 -> ALL 꺼짐)")
    void updateAlarmSetting_Sync_DisableOne() {
        // given
        // 모두 켜져있는 상태
        User user = User.builder()
                .allAlarm('Y').chatAlarm('Y').enrollAlarm('Y').eventAlarm('Y')
                .build();

        // when
        user.updateAlarmSetting(AlarmType.CHAT, 'N');

        // then
        // 하나라도 꺼졌으므로 ALL 알림은 N이 되어야 함
        assertThat(user.getChatAlarm()).isEqualTo('N');
        assertThat(user.getAllAlarm()).isEqualTo('N');
    }

    @Test
    @DisplayName("FCM 토큰 변경 감지 테스트")
    void isNewFcmToken() {
        // given
        User user = User.builder().fcmToken("oldToken").build();

        // when & then
        assertThat(user.isNewFcmToken("oldToken")).isFalse();
        assertThat(user.isNewFcmToken("newToken")).isTrue();
    }

    @Test
    @DisplayName("FCM 토큰 업데이트 및 삭제 테스트")
    void updateAndDeleteFcmToken() {
        // given
        User user = createUser();

        // when: update
        user.updateFcmToken("newToken");
        // then
        assertThat(user.getFcmToken()).isEqualTo("newToken");

        // when: delete
        user.deleteFcmToken();
        // then
        assertThat(user.getFcmToken()).isNull();
    }

    @Test
    @DisplayName("다른 유저인지 확인하는 로직 테스트")
    void isDifferentUserFrom() {
        // given
        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(2L).build();
        User user1Clone = User.builder().id(1L).build();

        // then
        assertThat(user1.isDifferentUserFrom(user2)).isTrue();
        assertThat(user1.isDifferentUserFrom(user1Clone)).isFalse();
    }

    @Test
    @DisplayName("유저 탈퇴 처리 테스트 - 게시글 삭제 및 토큰 삭제, Soft Delete 확인")
    void deleteUser() {
        // given
        User user = createUser();
        // Board 객체를 Mocking하여 deleteBoard() 호출 여부 검증
        Board mockBoard1 = mock(Board.class);
        Board mockBoard2 = mock(Board.class);

        // User의 boardList에 Mock 객체 추가
        user.getBoardList().add(mockBoard1);
        user.getBoardList().add(mockBoard2);

        // when
        user.deleteUser();

        // then
        // 1. BaseTimeEntity의 delete()가 호출되어 deletedAt이 설정되었는지 확인
        assertThat(user.getDeletedAt()).isNotNull();

        // 2. FCM 토큰이 삭제되었는지 확인
        assertThat(user.getFcmToken()).isNull();

        // 3. 연관된 게시글들의 deleteBoard()가 호출되었는지 확인
        verify(mockBoard1, times(1)).deleteBoard();
        verify(mockBoard2, times(1)).deleteBoard();
    }

    // --- Helper Method ---
    private User createUser() {
        return User.builder()
                .id(1L)
                .email("test@test.com")
                .provider(Provider.GOOGLE)
                .providerId("123")
                .nickName("tester")
                .gender('M')
                .birthDate(LocalDate.now())
                .profileImageUrl("original_image.jpg")
                .allAlarm('Y').chatAlarm('Y').enrollAlarm('Y').eventAlarm('Y')
                .fcmToken("token")
                .authority(Authority.ROLE_USER)
                .isReported(false)
                .build();
    }
}
