package com.itsm.core.repository.servicerequest;

import com.itsm.core.domain.servicerequest.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

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
}
