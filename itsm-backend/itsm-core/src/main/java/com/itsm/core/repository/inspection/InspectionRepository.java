package com.itsm.core.repository.inspection;

import com.itsm.core.domain.inspection.Inspection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface InspectionRepository extends JpaRepository<Inspection, Long> {

    List<Inspection> findByStatusCdAndScheduledAtBetween(String statusCd, LocalDate from, LocalDate to);

    List<Inspection> findByStatusCdAndScheduledAtBefore(String statusCd, LocalDate date);

    @Query("SELECT i FROM Inspection i WHERE " +
            "(:keyword IS NULL OR i.title LIKE %:keyword% OR i.description LIKE %:keyword%) " +
            "AND (:companyId IS NULL OR i.company.companyId = :companyId) " +
            "AND (:statusCd IS NULL OR i.statusCd = :statusCd) " +
            "AND (:inspectionTypeCd IS NULL OR i.inspectionTypeCd = :inspectionTypeCd)")
    Page<Inspection> search(@Param("keyword") String keyword,
                            @Param("companyId") Long companyId,
                            @Param("statusCd") String statusCd,
                            @Param("inspectionTypeCd") String inspectionTypeCd,
                            Pageable pageable);

    @Query("SELECT COUNT(i) FROM Inspection i WHERE i.statusCd = :statusCd")
    long countByStatusCd(@Param("statusCd") String statusCd);
}
