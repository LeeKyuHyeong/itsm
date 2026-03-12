package com.itsm.api.service.incident;

import com.itsm.api.dto.incident.DashboardStatsResponse;
import com.itsm.api.dto.incident.IncidentResponse;
import com.itsm.core.domain.incident.Incident;
import com.itsm.core.repository.incident.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final IncidentRepository incidentRepository;

    public DashboardStatsResponse getStats() {
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (String status : List.of("RECEIVED", "IN_PROGRESS", "COMPLETED", "CLOSED", "REJECTED")) {
            statusCounts.put(status, incidentRepository.countByStatusCd(status));
        }

        Map<String, Long> priorityCounts = new LinkedHashMap<>();
        for (String priority : List.of("CRITICAL", "HIGH", "MEDIUM", "LOW")) {
            long count = incidentRepository.countByStatusCdAndPriorityCd("RECEIVED", priority)
                    + incidentRepository.countByStatusCdAndPriorityCd("IN_PROGRESS", priority);
            priorityCounts.put(priority, count);
        }

        LocalDateTime now = LocalDateTime.now();
        List<Incident> activeIncidents = incidentRepository.findByStatusCd("RECEIVED");
        activeIncidents.addAll(incidentRepository.findByStatusCd("IN_PROGRESS"));

        long slaOverdueCount = activeIncidents.stream()
                .filter(i -> i.getSlaDeadlineAt() != null && i.getSlaDeadlineAt().isBefore(now))
                .count();

        long slaWarningCount = activeIncidents.stream()
                .filter(i -> i.getSlaDeadlineAt() != null && i.getSlaDeadlineAt().isAfter(now))
                .filter(i -> {
                    long total = Duration.between(i.getOccurredAt(), i.getSlaDeadlineAt()).toMinutes();
                    long elapsed = Duration.between(i.getOccurredAt(), now).toMinutes();
                    return total > 0 && ((double) elapsed / total * 100) >= 80;
                })
                .count();

        List<IncidentResponse> recentIncidents = incidentRepository
                .findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent().stream()
                .map(i -> IncidentResponse.builder()
                        .incidentId(i.getIncidentId())
                        .title(i.getTitle())
                        .statusCd(i.getStatusCd())
                        .priorityCd(i.getPriorityCd())
                        .companyNm(i.getCompany() != null ? i.getCompany().getCompanyNm() : null)
                        .occurredAt(i.getOccurredAt())
                        .createdAt(i.getCreatedAt())
                        .build())
                .toList();

        return DashboardStatsResponse.builder()
                .statusCounts(statusCounts)
                .priorityCounts(priorityCounts)
                .slaOverdueCount(slaOverdueCount)
                .slaWarningCount(slaWarningCount)
                .recentIncidents(recentIncidents)
                .build();
    }
}
