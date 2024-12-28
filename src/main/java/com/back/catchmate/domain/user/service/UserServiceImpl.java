package com.back.catchmate.domain.user.service;

import com.back.catchmate.domain.club.converter.ClubConverter;
import com.back.catchmate.domain.club.dto.ClubResponse.ClubInfo;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.user.converter.UserConverter;
import com.back.catchmate.domain.user.dto.UserRequest.UserJoinRequest;
import com.back.catchmate.domain.user.dto.UserRequest.UserProfileUpdateRequest;
import com.back.catchmate.domain.user.dto.UserResponse;
import com.back.catchmate.domain.user.dto.UserResponse.UpdateAlarmInfo;
import com.back.catchmate.domain.user.dto.UserResponse.UserInfo;
import com.back.catchmate.domain.user.entity.AlarmType;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.auth.entity.RefreshToken;
import com.back.catchmate.global.auth.repository.RefreshTokenRedisRepository;
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

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    public final String PROVIDER_ID_SEPARATOR = "@";
    private final JwtService jwtService;
    private final S3Service s3Service;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final UserConverter userConverter;
    private final ClubConverter clubConverter;

    @Override
    @Transactional
    public UserResponse.LoginInfo joinUser(UserJoinRequest request) {
        String providerIdWithProvider = request.getProviderId() + PROVIDER_ID_SEPARATOR + request.getProvider();
        Club favoreiteClub = clubRepository.findById(request.getFavoriteClubId())
                .orElseThrow(() -> new BaseException(ErrorCode.CLUB_NOT_FOUND));

        User user = userConverter.toEntity(request, favoreiteClub, providerIdWithProvider);

        // 이미 가입된 유저의 경우 예외 처리
        if (userRepository.existsByProviderId(providerIdWithProvider)) {
            throw new BaseException(ErrorCode.USER_ALREADY_EXIST);
        }

        // DB에 회원정보 저장
        userRepository.save(user);

        // accessToken, refreshToken 발급
        String accessToken = jwtService.createAccessToken(user.getId());
        String refreshToken = jwtService.createRefreshToken(user.getId());
        refreshTokenRedisRepository.save(RefreshToken.of(refreshToken, user.getId()));

        return userConverter.toLoginInfo(user, accessToken, refreshToken);
    }

    // 나의 프로필 정보를 가져오는 메서드
    @Override
    @Transactional(readOnly = true)
    public UserInfo getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        ClubInfo clubInfo = clubConverter.toClubInfo(user.getClub());
        return userConverter.toUserInfo(user, clubInfo);
    }

    // 다른 사용자의 프로필 정보를 가져오는 메서드
    @Override
    @Transactional(readOnly = true)
    public UserInfo getOtherUserProfile(Long userId, Long profileUserId) {
        User user = userRepository.findById(profileUserId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        ClubInfo clubInfo = clubConverter.toClubInfo(user.getClub());
        return userConverter.toUserInfo(user, clubInfo);
    }

    @Override
    @Transactional
    public StateResponse updateProfile(UserProfileUpdateRequest request, MultipartFile profileImage, Long userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        Club club = clubRepository.findById(request.getFavoriteClubId())
                .orElseThrow(() -> new BaseException(ErrorCode.CLUB_NOT_FOUND));
        user.updateProfile(request, club);

        String profileImageUrl = s3Service.uploadFile(profileImage);
        user.updateProfileImageUrl(profileImageUrl);
        return new StateResponse(true);
    }

    // 알람 수신 여부를 수정하는 메서드
    @Override
    @Transactional
    public UpdateAlarmInfo updateAlarm(Long userId, AlarmType alarmType, char isEnabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        user.updateAlarmSetting(alarmType, isEnabled);
        return userConverter.toUpdateAlarmInfo(user, alarmType, isEnabled);
    }
}
