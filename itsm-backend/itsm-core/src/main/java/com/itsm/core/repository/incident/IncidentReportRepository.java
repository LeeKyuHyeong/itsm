package com.itsm.core.repository.incident;

import com.itsm.core.domain.incident.IncidentReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IncidentReportRepository extends JpaRepository<IncidentReport, Long> {

    Optional<IncidentReport> findByIncidentId(Long incidentId);
}
