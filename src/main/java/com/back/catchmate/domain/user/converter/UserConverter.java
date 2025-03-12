package com.back.catchmate.domain.user.converter;

import com.back.catchmate.domain.club.converter.ClubConverter;
import com.back.catchmate.domain.club.dto.ClubResponse.ClubInfo;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.user.dto.UserRequest;
import com.back.catchmate.domain.user.dto.UserResponse;
import com.back.catchmate.domain.user.dto.UserResponse.LoginInfo;
import com.back.catchmate.domain.user.dto.UserResponse.UnreadStatusInfo;
import com.back.catchmate.domain.user.dto.UserResponse.UpdateAlarmInfo;
import com.back.catchmate.domain.user.dto.UserResponse.UserInfo;
import com.back.catchmate.domain.user.entity.AlarmType;
import com.back.catchmate.domain.user.entity.Authority;
import com.back.catchmate.domain.user.entity.BlockedUser;
import com.back.catchmate.domain.user.entity.Provider;
import com.back.catchmate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserConverter {
    private final ClubConverter clubConverter;

    public User toEntity(UserRequest.UserJoinRequest request, Club favoriteClub, String providerIdWithProvider) {
        String watchStyle = request.getWatchStyle().isEmpty()
                ? null // 기본값 설정
                : request.getWatchStyle();

        return User.builder()
                .email(request.getEmail())
                .provider(Provider.of(request.getProvider()))
                .providerId(providerIdWithProvider)
                .gender(request.getGender())
                .nickName(request.getNickName())
                .birthDate(request.getBirthDate())
                .club(favoriteClub)
                .watchStyle(watchStyle)
                .profileImageUrl(request.getProfileImageUrl())
                .allAlarm('N')
                .chatAlarm('N')
                .enrollAlarm('N')
                .eventAlarm('N')
                .fcmToken(request.getFcmToken())
                .authority(Authority.ROLE_USER)
                .build();
    }

    public LoginInfo toLoginInfo(User user, String accessToken, String refreshToken) {
        return LoginInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public UserInfo toUserInfo(User user) {
        ClubInfo clubInfo = clubConverter.toClubInfo(user.getClub());

        return UserInfo.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .gender(user.getGender())
                .allAlarm(user.getAllAlarm())
                .chatAlarm(user.getChatAlarm())
                .enrollAlarm(user.getEnrollAlarm())
                .eventAlarm(user.getEventAlarm())
                .nickName(user.getNickName())
                .favoriteClub(clubInfo)
                .birthDate(user.getBirthDate())
                .watchStyle(user.getWatchStyle())
                .build();
    }

    public UserResponse.PagedUserInfo toPagedUserInfo(Page<BlockedUser> blockedUserList) {
        List<UserResponse.UserInfo> blockedUserInfoList = blockedUserList.stream()
                .map(blockedUser -> toUserInfo(blockedUser.getBlocked()))  // 차단된 유저 정보 변환
                .collect(Collectors.toList());

        return UserResponse.PagedUserInfo.builder()
                .userInfoList(blockedUserInfoList)
                .totalPages(blockedUserList.getTotalPages())
                .totalElements(blockedUserList.getTotalElements())
                .isFirst(blockedUserList.isFirst())
                .isLast(blockedUserList.isLast())
                .build();
    }

    public UpdateAlarmInfo toUpdateAlarmInfo(User user, AlarmType alarmType, char isEnabled) {
        return UpdateAlarmInfo.builder()
                .userId(user.getId())
                .alarmType(alarmType)
                .isEnabled(isEnabled)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public UnreadStatusInfo toUnreadStatusInfo(Boolean hasUnreadNotification, Boolean hasUnreadChat) {
        return UnreadStatusInfo.builder()
                .hasUnreadNotification(hasUnreadNotification)
                .hasUnreadChat(hasUnreadChat)
                .build();
    }
}
