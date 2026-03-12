package com.itsm.core.repository.servicerequest;

import com.itsm.core.domain.servicerequest.ServiceRequestAssignee;
import com.itsm.core.domain.servicerequest.ServiceRequestAssigneeId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRequestAssigneeRepository extends JpaRepository<ServiceRequestAssignee, ServiceRequestAssigneeId> {
    List<ServiceRequestAssignee> findByRequestId(Long requestId);
}
