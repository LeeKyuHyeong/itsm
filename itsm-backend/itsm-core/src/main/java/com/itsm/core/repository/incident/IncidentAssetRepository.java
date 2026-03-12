package com.itsm.core.repository.incident;

import com.itsm.core.domain.incident.IncidentAsset;
import com.itsm.core.domain.incident.IncidentAssetId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentAssetRepository extends JpaRepository<IncidentAsset, IncidentAssetId> {

    List<IncidentAsset> findByIncidentId(Long incidentId);

    List<IncidentAsset> findByAssetTypeAndAssetId(String assetType, Long assetId);
}
