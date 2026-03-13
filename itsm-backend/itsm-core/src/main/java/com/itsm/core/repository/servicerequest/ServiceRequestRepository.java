package com.itsm.core.repository.servicerequest;

import com.itsm.core.domain.servicerequest.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    List<ServiceRequest> findByStatusCd(String statusCd);

    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.statusCd = :statusCd AND sr.updatedAt < :before")
    List<ServiceRequest> findByStatusCdAndUpdatedAtBefore(@Param("statusCd") String statusCd,
                                                          @Param("before") LocalDateTime before);

    @Query("SELECT sr FROM ServiceRequest sr WHERE " +
            "(:keyword IS NULL OR sr.title LIKE %:keyword% OR sr.content LIKE %:keyword%) " +
            "AND (:companyId IS NULL OR sr.company.companyId = :companyId) " +
            "AND (:statusCd IS NULL OR sr.statusCd = :statusCd) " +
            "AND (:priorityCd IS NULL OR sr.priorityCd = :priorityCd) " +
            "AND (:requestTypeCd IS NULL OR sr.requestTypeCd = :requestTypeCd)")
    Page<ServiceRequest> search(@Param("keyword") String keyword,
                                @Param("companyId") Long companyId,
                                @Param("statusCd") String statusCd,
                                @Param("priorityCd") String priorityCd,
                                @Param("requestTypeCd") String requestTypeCd,
                                Pageable pageable);

    @Query("SELECT COUNT(sr) FROM ServiceRequest sr WHERE sr.statusCd = :statusCd")
    long countByStatusCd(@Param("statusCd") String statusCd);

    @Query("SELECT COUNT(sr) FROM ServiceRequest sr WHERE sr.createdAt >= :from AND sr.createdAt < :to")
    long countByCreatedAtBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
