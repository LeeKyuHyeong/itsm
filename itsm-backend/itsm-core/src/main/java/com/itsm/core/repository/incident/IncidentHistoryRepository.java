package com.itsm.core.repository.incident;

import com.itsm.core.domain.incident.IncidentHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentHistoryRepository extends JpaRepository<IncidentHistory, Long> {

    List<IncidentHistory> findByIncidentIdOrderByCreatedAtDesc(Long incidentId);

    Page<IncidentHistory> findByIncidentIdOrderByCreatedAtDesc(Long incidentId, Pageable pageable);
}
