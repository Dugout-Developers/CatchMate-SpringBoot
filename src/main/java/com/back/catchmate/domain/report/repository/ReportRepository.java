package com.back.catchmate.domain.report.repository;

import com.back.catchmate.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
