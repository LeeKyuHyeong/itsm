package com.itsm.core.repository.servicerequest;

import com.itsm.core.domain.servicerequest.ServiceRequestAsset;
import com.itsm.core.domain.servicerequest.ServiceRequestAssetId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRequestAssetRepository extends JpaRepository<ServiceRequestAsset, ServiceRequestAssetId> {
    List<ServiceRequestAsset> findByRequestId(Long requestId);
}
