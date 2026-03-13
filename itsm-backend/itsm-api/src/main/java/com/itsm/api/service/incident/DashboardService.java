package com.itsm.api.service.incident;

import com.itsm.api.dto.incident.DashboardStatsResponse;
import com.itsm.api.dto.incident.IncidentResponse;
import com.itsm.api.dto.incident.MonthlyTrendItem;
import com.itsm.core.domain.incident.Incident;
import com.itsm.core.repository.change.ChangeRepository;
import com.itsm.core.repository.incident.IncidentRepository;
import com.itsm.core.repository.inspection.InspectionRepository;
import com.itsm.core.repository.servicerequest.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final IncidentRepository incidentRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ChangeRepository changeRepository;
    private final InspectionRepository inspectionRepository;

    private static final List<String> INCIDENT_STATUSES = List.of(
            "RECEIVED", "IN_PROGRESS", "COMPLETED", "CLOSED", "REJECTED");
    private static final List<String> PRIORITY_CODES = List.of(
            "CRITICAL", "HIGH", "MEDIUM", "LOW");
    private static final List<String> SR_STATUSES = List.of(
            "RECEIVED", "ASSIGNED", "IN_PROGRESS", "PENDING_COMPLETE", "CLOSED", "CANCELLED", "REJECTED");
    private static final List<String> CHANGE_STATUSES = List.of(
            "DRAFT", "APPROVAL_REQUESTED", "APPROVED", "IN_PROGRESS", "COMPLETED", "CLOSED", "REJECTED", "CANCELLED");
    private static final List<String> INSPECTION_STATUSES = List.of(
            "SCHEDULED", "IN_PROGRESS", "ON_HOLD", "COMPLETED", "CLOSED");

    public DashboardStatsResponse getStats() {
        // --- Incident stats ---
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        long totalIncident = 0;
        for (String status : INCIDENT_STATUSES) {
            long count = incidentRepository.countByStatusCd(status);
            statusCounts.put(status, count);
            totalIncident += count;
        }

        Map<String, Long> priorityCounts = new LinkedHashMap<>();
        for (String priority : PRIORITY_CODES) {
            long count = incidentRepository.countByStatusCdAndPriorityCd("RECEIVED", priority)
                    + incidentRepository.countByStatusCdAndPriorityCd("IN_PROGRESS", priority);
            priorityCounts.put(priority, count);
        }

        LocalDateTime now = LocalDateTime.now();
        List<Incident> activeIncidents = new ArrayList<>(incidentRepository.findByStatusCd("RECEIVED"));
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

        // --- SR stats ---
        Map<String, Long> srStatusCounts = new LinkedHashMap<>();
        long totalSr = 0;
        for (String status : SR_STATUSES) {
            long count = serviceRequestRepository.countByStatusCd(status);
            srStatusCounts.put(status, count);
            totalSr += count;
        }
        long pendingSrCount = srStatusCounts.getOrDefault("PENDING_COMPLETE", 0L);

        // --- Change stats ---
        Map<String, Long> changeStatusCounts = new LinkedHashMap<>();
        long totalChange = 0;
        for (String status : CHANGE_STATUSES) {
            long count = changeRepository.countByStatusCd(status);
            changeStatusCounts.put(status, count);
            totalChange += count;
        }

        // --- Inspection stats ---
        Map<String, Long> inspectionStatusCounts = new LinkedHashMap<>();
        long totalInspection = 0;
        for (String status : INSPECTION_STATUSES) {
            long count = inspectionRepository.countByStatusCd(status);
            inspectionStatusCounts.put(status, count);
            totalInspection += count;
        }

        // --- Monitoring metrics ---
        long unassignedIncidentCount = incidentRepository.countUnassigned();

        // --- Monthly trend (last 6 months) ---
        List<MonthlyTrendItem> monthlyTrend = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = currentMonth.minusMonths(i);
            LocalDateTime from = ym.atDay(1).atStartOfDay();
            LocalDateTime to = ym.plusMonths(1).atDay(1).atStartOfDay();

            monthlyTrend.add(MonthlyTrendItem.builder()
                    .month(ym.format(formatter))
                    .incidentCount(incidentRepository.countByCreatedAtBetween(from, to))
                    .srCount(serviceRequestRepository.countByCreatedAtBetween(from, to))
                    .changeCount(changeRepository.countByCreatedAtBetween(from, to))
                    .build());
        }

        return DashboardStatsResponse.builder()
                .statusCounts(statusCounts)
                .priorityCounts(priorityCounts)
                .slaOverdueCount(slaOverdueCount)
                .slaWarningCount(slaWarningCount)
                .recentIncidents(recentIncidents)
                .srStatusCounts(srStatusCounts)
                .changeStatusCounts(changeStatusCounts)
                .inspectionStatusCounts(inspectionStatusCounts)
                .unassignedIncidentCount(unassignedIncidentCount)
                .delayedIncidentCount(slaOverdueCount)
                .pendingSrCount(pendingSrCount)
                .totalIncidentCount(totalIncident)
                .totalSrCount(totalSr)
                .totalChangeCount(totalChange)
                .totalInspectionCount(totalInspection)
                .monthlyTrend(monthlyTrend)
                .build();
    }
}
