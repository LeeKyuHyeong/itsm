package com.itsm.core.repository.report;

import com.itsm.core.domain.report.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByRefTypeAndRefId(String refType, Long refId);

    @Query("SELECT r FROM Report r JOIN FETCH r.reportForm WHERE " +
            "(:refType IS NULL OR r.refType = :refType) " +
            "AND (:refId IS NULL OR r.refId = :refId)")
    Page<Report> search(@Param("refType") String refType,
                        @Param("refId") Long refId,
                        Pageable pageable);
}
