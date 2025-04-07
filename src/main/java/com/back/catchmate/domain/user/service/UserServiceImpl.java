package com.back.catchmate.domain.user.service;

import com.back.catchmate.domain.chat.service.UserChatRoomService;
import com.back.catchmate.domain.club.converter.ClubConverter;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.notification.service.NotificationService;
import com.back.catchmate.domain.user.converter.UserConverter;
import com.back.catchmate.domain.user.dto.UserRequest.UserJoinRequest;
import com.back.catchmate.domain.user.dto.UserRequest.UserProfileUpdateRequest;
import com.back.catchmate.domain.user.dto.UserResponse.LoginInfo;
import com.back.catchmate.domain.user.dto.UserResponse.UnreadStatusInfo;
import com.back.catchmate.domain.user.dto.UserResponse.UpdateAlarmInfo;
import com.back.catchmate.domain.user.dto.UserResponse.UserInfo;
import com.back.catchmate.domain.user.entity.AlarmType;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.auth.entity.RefreshToken;
import com.back.catchmate.global.auth.repository.RefreshTokenRepository;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import com.back.catchmate.global.jwt.JwtService;
import com.back.catchmate.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.back.catchmate.global.auth.service.AuthServiceImpl.PROVIDER_ID_SEPARATOR;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final S3Service s3Service;
    private final JwtService jwtService;
    private final NotificationService notificationService;
    private final UserChatRoomService userChatRoomService;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserConverter userConverter;

    @Override
    @Transactional
    public LoginInfo joinUser(UserJoinRequest request) {
        String providerIdWithProvider = generateProviderId(request);

        validateUserDoesNotExist(providerIdWithProvider);

        Club favoriteClub = getFavoriteClub(request.getFavoriteClubId());
        User user = userConverter.toEntity(request, favoriteClub, providerIdWithProvider);

        userRepository.save(user);

        return createLoginInfoForUser(user);
    }

    private String generateProviderId(UserJoinRequest request) {
        return request.getProviderId() + PROVIDER_ID_SEPARATOR + request.getProvider();
    }

    private void validateUserDoesNotExist(String providerIdWithProvider) {
        if (userRepository.existsByProviderIdAndDeletedAtIsNull(providerIdWithProvider)) {
            throw new BaseException(ErrorCode.USER_ALREADY_EXIST);
        }
    }

    private Club getFavoriteClub(Long favoriteClubId) {
        return clubRepository.findById(favoriteClubId)
                .orElseThrow(() -> new BaseException(ErrorCode.CLUB_NOT_FOUND));
    }

    private LoginInfo createLoginInfoForUser(User user) {
        String accessToken = jwtService.createAccessToken(user.getId());
        String refreshToken = jwtService.createRefreshToken(user.getId());
        refreshTokenRepository.save(RefreshToken.of(refreshToken, user.getId()));
        return userConverter.toLoginInfo(user, accessToken, refreshToken);
    }

    @Override
    public UserInfo getMyProfile(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        return userConverter.toUserInfo(user);
    }

    @Override
    public UserInfo getOtherUserProfile(Long userId, Long profileUserId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(profileUserId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        return userConverter.toUserInfo(user);
    }

    @Override
    @Transactional
    public StateResponse updateProfile(UserProfileUpdateRequest request, MultipartFile profileImage, Long userId) throws IOException {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Club club = clubRepository.findById(request.getFavoriteClubId())
                .orElseThrow(() -> new BaseException(ErrorCode.CLUB_NOT_FOUND));

        user.updateProfile(request, club);

        String profileImageUrl = s3Service.uploadFile(profileImage);
        user.updateProfileImageUrl(profileImageUrl);
        return new StateResponse(true);
    }

    @Override
    @Transactional
    public UpdateAlarmInfo updateAlarm(Long userId, AlarmType alarmType, char isEnabled) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        user.updateAlarmSetting(alarmType, isEnabled);
        return userConverter.toUpdateAlarmInfo(user, alarmType, isEnabled);
    }

    @Override
    @Transactional
    public StateResponse deleteUser(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        user.deleteUser();
        return new StateResponse(true);
    }

    @Override
    public UnreadStatusInfo hasUnreadMessagesOrNotifications(Long userId) {
        Boolean hasUnreadNotification = notificationService.hasUnreadNotification(userId);
        Boolean hasUnreadChat = userChatRoomService.hasUnreadChat(userId);

        return userConverter.toUnreadStatusInfo(hasUnreadNotification, hasUnreadChat);
    }
}
