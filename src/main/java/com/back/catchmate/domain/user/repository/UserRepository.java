package com.back.catchmate.domain.user.repository;

import com.back.catchmate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByProviderId(String providerId);

    Optional<User> findByProviderId(String providerId);

    boolean existsByNickName(String nickName);
}
