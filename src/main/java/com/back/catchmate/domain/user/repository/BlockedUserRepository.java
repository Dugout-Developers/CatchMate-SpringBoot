package com.back.catchmate.domain.user.repository;

import com.back.catchmate.domain.user.entity.BlockedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlockedUserRepository extends JpaRepository<BlockedUser, Long> {
    boolean existsByBlockerIdAndBlockedIdAndDeletedAtIsNull(Long blockerId, Long blockedId);

    Optional<BlockedUser> findByBlockerIdAndBlockedIdAndDeletedAtIsNull(Long blockerId, Long blockedId);

    Page<BlockedUser> findAllByBlockerIdAndDeletedAtIsNull(Long blockerId, Pageable pageable);

    @Query("SELECT b.blocked.id FROM BlockedUser b WHERE b.blocker.id = :userId AND b.deletedAt IS NULL")
    List<Long> findBlockedUserIdListByUserId(@Param("userId") Long userId);
}
