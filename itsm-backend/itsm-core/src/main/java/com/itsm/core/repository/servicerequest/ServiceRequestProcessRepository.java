package com.itsm.core.repository.servicerequest;

import com.itsm.core.domain.servicerequest.ServiceRequestProcess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRequestProcessRepository extends JpaRepository<ServiceRequestProcess, Long> {
    List<ServiceRequestProcess> findByRequestIdOrderByCreatedAtAsc(Long requestId);
    List<ServiceRequestProcess> findByRequestIdAndUserId(Long requestId, Long userId);
}
