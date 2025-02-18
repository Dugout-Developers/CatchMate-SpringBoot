package com.back.catchmate.domain.inquiry.repository;

import com.back.catchmate.domain.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    long countByDeletedAtIsNull();
}
