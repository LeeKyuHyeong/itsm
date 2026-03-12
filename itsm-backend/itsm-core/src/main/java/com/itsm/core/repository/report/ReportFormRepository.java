package com.itsm.core.repository.report;

import com.itsm.core.domain.report.ReportForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportFormRepository extends JpaRepository<ReportForm, Long> {

    @Query("SELECT rf FROM ReportForm rf WHERE " +
            "(:keyword IS NULL OR rf.formNm LIKE %:keyword%) " +
            "AND (:formTypeCd IS NULL OR rf.formTypeCd = :formTypeCd) " +
            "AND (:isActive IS NULL OR rf.isActive = :isActive)")
    Page<ReportForm> search(@Param("keyword") String keyword,
                            @Param("formTypeCd") String formTypeCd,
                            @Param("isActive") String isActive,
                            Pageable pageable);
}
