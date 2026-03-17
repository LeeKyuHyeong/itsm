package com.itsm.core.repository.incident;

import com.itsm.core.domain.incident.IncidentAssignee;
import com.itsm.core.domain.incident.IncidentAssigneeId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentAssigneeRepository extends JpaRepository<IncidentAssignee, IncidentAssigneeId> {

    @EntityGraph(attributePaths = {"user"})
    List<IncidentAssignee> findByIncidentId(Long incidentId);

    List<IncidentAssignee> findByUserId(Long userId);
}
