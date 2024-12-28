package com.back.catchmate.domain.user.service;

import com.back.catchmate.domain.user.dto.UserRequest;
import com.back.catchmate.domain.user.dto.UserRequest.UserProfileUpdateRequest;
import com.back.catchmate.domain.user.dto.UserResponse.LoginInfo;
import com.back.catchmate.domain.user.entity.AlarmType;
import com.back.catchmate.global.dto.StateResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.back.catchmate.domain.user.dto.UserResponse.*;

public interface UserService {
    LoginInfo joinUser(UserRequest.UserJoinRequest request);

    UserInfo getMyProfile(Long userId);

    UserInfo getOtherUserProfile(Long userId, Long profileUserId);

    StateResponse updateProfile(UserProfileUpdateRequest request, MultipartFile profileImage, Long userId) throws IOException, IOException;

    UpdateAlarmInfo updateAlarm(Long userId, AlarmType alarmType, char isEnabled);
}
