package com.itsm.api.service.incident;

import com.itsm.api.dto.incident.DashboardStatsResponse;
import com.itsm.api.dto.incident.IncidentResponse;
import com.itsm.api.dto.incident.MonthlyTrendItem;
import com.itsm.core.constant.IncidentStatus;
import com.itsm.core.constant.ServiceRequestStatus;
import com.itsm.core.repository.change.ChangeRepository;
import com.itsm.core.repository.incident.IncidentRepository;
import com.itsm.core.repository.inspection.InspectionRepository;
import com.itsm.core.repository.servicerequest.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static final List<String> PRIORITY_CODES = List.of(
            "CRITICAL", "HIGH", "MEDIUM", "LOW");
    private static final List<String> CHANGE_STATUSES = List.of(
            "DRAFT", "APPROVAL_REQUESTED", "APPROVED", "IN_PROGRESS", "COMPLETED", "CLOSED", "REJECTED", "CANCELLED");
    private static final List<String> INSPECTION_STATUSES = List.of(
            "SCHEDULED", "IN_PROGRESS", "ON_HOLD", "COMPLETED", "CLOSED");

    public DashboardStatsResponse getStats() {
        LocalDateTime now = LocalDateTime.now();

        // --- Incident stats (1 query instead of 5) ---
        Map<String, Long> statusCounts = buildCountMap(IncidentStatus.ALL, incidentRepository.countGroupByStatusCd());
        long totalIncident = statusCounts.values().stream().mapToLong(Long::longValue).sum();

        // --- Priority stats (1 query instead of 8) ---
        Map<String, Long> priorityCounts = buildCountMap(PRIORITY_CODES, incidentRepository.countActivePriorityGrouped());

        // --- SLA stats (2 queries instead of loading all entities + Java filter) ---
        long slaOverdueCount = incidentRepository.countSlaOverdue(now);
        long slaWarningCount = incidentRepository.countSlaWarning(now);

        // --- Recent incidents (JOIN FETCH company to avoid N+1) ---
        List<IncidentResponse> recentIncidents = incidentRepository
                .findRecentWithCompany(PageRequest.of(0, 10))
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

        // --- SR stats (1 query instead of 7) ---
        Map<String, Long> srStatusCounts = buildCountMap(ServiceRequestStatus.ALL, serviceRequestRepository.countGroupByStatusCd());
        long totalSr = srStatusCounts.values().stream().mapToLong(Long::longValue).sum();
        long pendingSrCount = srStatusCounts.getOrDefault("PENDING_COMPLETE", 0L);

        // --- Change stats (1 query instead of 8) ---
        Map<String, Long> changeStatusCounts = buildCountMap(CHANGE_STATUSES, changeRepository.countGroupByStatusCd());
        long totalChange = changeStatusCounts.values().stream().mapToLong(Long::longValue).sum();

        // --- Inspection stats (1 query instead of 5) ---
        Map<String, Long> inspectionStatusCounts = buildCountMap(INSPECTION_STATUSES, inspectionRepository.countGroupByStatusCd());
        long totalInspection = inspectionStatusCounts.values().stream().mapToLong(Long::longValue).sum();

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

    private Map<String, Long> buildCountMap(List<String> allStatuses, List<Object[]> queryResults) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (String status : allStatuses) {
            map.put(status, 0L);
        }
        for (Object[] row : queryResults) {
            String status = (String) row[0];
            Long count = (Long) row[1];
            map.put(status, count);
        }
        return map;
    }
}
