package com.back.catchmate.domain.user.repository;

import com.back.catchmate.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u.club.id, COUNT(u) FROM User u WHERE u.deletedAt IS NULL GROUP BY u.club.id")
    List<Object[]> countUsersByClub();

    long countByDeletedAtIsNull();

    @Query("SELECT u.watchStyle, COUNT(u) FROM User u WHERE u.deletedAt IS NULL GROUP BY u.watchStyle")
    List<Object[]> countUsersByWatchStyle();

    boolean existsByNickName(String nickName);

    long countByGenderAndDeletedAtIsNull(Character gender);

    Optional<User> findByIdAndDeletedAtIsNull(Long userId);

    Page<User> findAllByDeletedAtIsNull(Pageable pageable);

    boolean existsByProviderIdAndDeletedAtIsNull(String providerId);

    Optional<User> findByProviderIdAndDeletedAtIsNull(String providerId);

    Page<User> findByClubNameAndDeletedAtIsNull(String clubName, Pageable pageable);
}
