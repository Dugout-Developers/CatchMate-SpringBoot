package com.back.catchmate.domain.user.service;

import com.back.catchmate.domain.user.converter.BlockerUserConverter;
import com.back.catchmate.domain.user.converter.UserConverter;
import com.back.catchmate.domain.user.dto.UserResponse.PagedUserInfo;
import com.back.catchmate.domain.user.entity.BlockedUser;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.BlockedUserRepository;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BlockedUserServiceImpl implements BlockedUserService {
    private final UserRepository userRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final BlockerUserConverter blockerUserConverter;
    private final UserConverter userConverter;

    @Override
    @Transactional
    public StateResponse blockUser(Long blockerId, Long blockedUserId) {
        validateBlockRequest(blockerId, blockedUserId);

        User blocker = getUserOrThrow(blockerId);
        User blocked = getUserOrThrow(blockedUserId);

        BlockedUser blockedUser = blockerUserConverter.toEntity(blocker, blocked);
        blockedUserRepository.save(blockedUser);

        return new StateResponse(true);
    }

    @Override
    @Transactional
    public StateResponse unblockUser(Long blockerId, Long blockedUserId) {
        BlockedUser blockedUser = blockedUserRepository.findByBlockerIdAndBlockedIdAndDeletedAtIsNull(blockerId, blockedUserId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_UNBLOCK_FAILED));

        blockedUser.delete();
        return new StateResponse(true);
    }

    @Override
    public PagedUserInfo getBlockedUserList(Long blockerId, Pageable pageable) {
        User user = getUserOrThrow(blockerId);

        Page<BlockedUser> blockedUsers = blockedUserRepository
                .findAllByBlockerIdAndDeletedAtIsNull(user.getId(), pageable);

        return userConverter.toPagedBlockedUserInfo(blockedUsers);
    }

    private void validateBlockRequest(Long blockerId, Long blockedUserId) {
        if (blockerId.equals(blockedUserId)) {
            throw new BaseException(ErrorCode.SELF_BLOCK_FAILED);
        }

        boolean alreadyBlocked = blockedUserRepository.existsByBlockerIdAndBlockedIdAndDeletedAtIsNull(blockerId, blockedUserId);

        if (alreadyBlocked) {
            throw new BaseException(ErrorCode.USER_BLOCK_FAILED);
        }
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
    }
}
