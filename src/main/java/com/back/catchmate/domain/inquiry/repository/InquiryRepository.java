package com.back.catchmate.domain.inquiry.repository;

import com.back.catchmate.domain.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    long countByDeletedAtIsNull();

    Optional<Inquiry> findByIdAndDeletedAtIsNull(Long inquiryId);
}
