package com.back.catchmate.domain.user.service;

import com.back.catchmate.domain.user.dto.UserResponse.PagedUserInfo;
import com.back.catchmate.global.dto.StateResponse;
import org.springframework.data.domain.Pageable;

public interface BlockedUserService {
    StateResponse blockUser(Long blockerId, Long blockedUserId);

    StateResponse unblockUser(Long blockerId, Long blockedUserId);

    PagedUserInfo getBlockedUserList(Long blockerId, Pageable pageable);
}
