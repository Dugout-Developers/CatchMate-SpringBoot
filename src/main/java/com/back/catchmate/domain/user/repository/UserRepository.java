package com.back.catchmate.domain.user.repository;

import com.back.catchmate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByProviderId(String providerId);

    Optional<User> findByProviderId(String providerId);

    boolean existsByNickName(String nickName);

    long countByDeletedAtIsNull();

    long countByGenderAndDeletedAtIsNull(char gender);

    @Query("SELECT u.club.name, COUNT(u) FROM User u WHERE u.deletedAt IS NULL GROUP BY u.club.name")
    List<Object[]> countUsersByClub();

    @Query("SELECT u.watchStyle, COUNT(u) FROM User u WHERE u.deletedAt IS NULL GROUP BY u.watchStyle")
    List<Object[]> countUsersByWatchStyle();
}
