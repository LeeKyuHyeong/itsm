package com.itsm.core.repository.inspection;

import com.itsm.core.domain.inspection.InspectionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionItemRepository extends JpaRepository<InspectionItem, Long> {
    List<InspectionItem> findByInspectionIdOrderBySortOrderAsc(Long inspectionId);
    long countByInspectionId(Long inspectionId);
}
