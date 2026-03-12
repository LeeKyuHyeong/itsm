package com.itsm.core.repository.inspection;

import com.itsm.core.domain.inspection.InspectionAsset;
import com.itsm.core.domain.inspection.InspectionAssetId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionAssetRepository extends JpaRepository<InspectionAsset, InspectionAssetId> {
    List<InspectionAsset> findByInspectionId(Long inspectionId);
}
