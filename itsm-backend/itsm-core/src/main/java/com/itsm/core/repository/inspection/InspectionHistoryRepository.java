package com.itsm.core.repository.inspection;

import com.itsm.core.domain.inspection.InspectionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionHistoryRepository extends JpaRepository<InspectionHistory, Long> {
    List<InspectionHistory> findByInspectionIdOrderByCreatedAtDesc(Long inspectionId);
}
