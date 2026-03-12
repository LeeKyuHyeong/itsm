package com.itsm.core.repository.inspection;

import com.itsm.core.domain.inspection.InspectionResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InspectionResultRepository extends JpaRepository<InspectionResult, Long> {
    List<InspectionResult> findByInspectionId(Long inspectionId);
    Optional<InspectionResult> findByInspectionIdAndItemId(Long inspectionId, Long itemId);
    long countByInspectionId(Long inspectionId);
}
