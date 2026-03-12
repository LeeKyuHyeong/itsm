package com.itsm.core.repository.servicerequest;

import com.itsm.core.domain.servicerequest.ServiceRequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRequestHistoryRepository extends JpaRepository<ServiceRequestHistory, Long> {
    List<ServiceRequestHistory> findByRequestIdOrderByCreatedAtDesc(Long requestId);
}
