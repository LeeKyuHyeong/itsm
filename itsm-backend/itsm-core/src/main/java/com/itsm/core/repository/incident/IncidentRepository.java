package com.itsm.core.repository.incident;

import com.itsm.core.domain.incident.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findByStatusCdIn(List<String> statusCds);

    @Query("SELECT i FROM Incident i WHERE i.statusCd IN :statusCds AND i.mainManager IS NULL")
    List<Incident> findByStatusCdInAndMainManagerIsNull(@Param("statusCds") List<String> statusCds);

    @Query("SELECT i FROM Incident i WHERE " +
            "(:keyword IS NULL OR i.title LIKE %:keyword% OR i.content LIKE %:keyword%) " +
            "AND (:companyId IS NULL OR i.company.companyId = :companyId) " +
            "AND (:statusCd IS NULL OR i.statusCd = :statusCd) " +
            "AND (:priorityCd IS NULL OR i.priorityCd = :priorityCd) " +
            "AND (:incidentTypeCd IS NULL OR i.incidentTypeCd = :incidentTypeCd)")
    Page<Incident> search(@Param("keyword") String keyword,
                          @Param("companyId") Long companyId,
                          @Param("statusCd") String statusCd,
                          @Param("priorityCd") String priorityCd,
                          @Param("incidentTypeCd") String incidentTypeCd,
                          Pageable pageable);

    List<Incident> findByStatusCd(String statusCd);

    List<Incident> findByMainManagerUserId(Long managerId);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.statusCd = :statusCd")
    long countByStatusCd(@Param("statusCd") String statusCd);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.statusCd = :statusCd AND i.priorityCd = :priorityCd")
    long countByStatusCdAndPriorityCd(@Param("statusCd") String statusCd, @Param("priorityCd") String priorityCd);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.statusCd IN ('RECEIVED', 'IN_PROGRESS') AND i.mainManager IS NULL")
    long countUnassigned();

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.createdAt >= :from AND i.createdAt < :to")
    long countByCreatedAtBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
