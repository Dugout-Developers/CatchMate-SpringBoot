package com.back.catchmate.domain.user.repository;

import com.back.catchmate.domain.user.entity.BlockedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlockedUserRepository extends JpaRepository<BlockedUser, Long> {
    boolean existsByBlockerIdAndBlockedIdAndDeletedAtIsNull(Long blockerId, Long blockedId);

    Optional<BlockedUser> findByBlockerIdAndBlockedIdAndDeletedAtIsNull(Long blockerId, Long blockedId);

    Page<BlockedUser> findAllByBlockerIdAndDeletedAtIsNull(Long blockerId, Pageable pageable);
}
