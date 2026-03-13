package com.itsm.batch.job.statistics;

import com.itsm.core.domain.incident.Incident;
import com.itsm.core.domain.log.LoginHistory;
import com.itsm.core.domain.servicerequest.ServiceRequest;
import com.itsm.core.domain.statistics.DailyStatistics;
import com.itsm.core.repository.asset.AssetHwRepository;
import com.itsm.core.repository.asset.AssetSwRepository;
import com.itsm.core.repository.change.ChangeRepository;
import com.itsm.core.repository.incident.IncidentRepository;
import com.itsm.core.repository.log.LoginHistoryRepository;
import com.itsm.core.repository.log.SimMenuAccessLogRepository;
import com.itsm.core.repository.servicerequest.ServiceRequestRepository;
import com.itsm.core.repository.statistics.DailyStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsAggregationJob {

    private final DailyStatisticsRepository dailyStatisticsRepository;
    private final IncidentRepository incidentRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ChangeRepository changeRepository;
    private final AssetHwRepository assetHwRepository;
    private final AssetSwRepository assetSwRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final SimMenuAccessLogRepository menuAccessLogRepository;

    public void execute() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("통계 집계 시작: {}", yesterday);

        aggregateIncidentStats(yesterday);
        aggregateServiceRequestStats(yesterday);
        aggregateChangeStats(yesterday);
        aggregateAssetStats(yesterday);
        aggregateUserStats(yesterday);

        log.info("통계 집계 완료: {}", yesterday);
    }

    private void aggregateIncidentStats(LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        // total_count
        long totalCount = incidentRepository.countByCreatedAtBetween(dayStart, dayEnd);
        upsertStat(date, "INCIDENT", "total_count", new BigDecimal(totalCount), null);

        // completed_count & avg_resolution_hours & sla_compliance_rate
        List<Incident> completedIncidents = incidentRepository.findByStatusCd("COMPLETED");

        long completedCount = completedIncidents.stream()
                .filter(i -> i.getCompletedAt() != null
                        && !i.getCompletedAt().isBefore(dayStart)
                        && i.getCompletedAt().isBefore(dayEnd))
                .count();
        upsertStat(date, "INCIDENT", "completed_count", new BigDecimal(completedCount), null);

        // avg_resolution_hours
        List<Incident> completedWithTimes = completedIncidents.stream()
                .filter(i -> i.getOccurredAt() != null && i.getCompletedAt() != null)
                .toList();

        if (!completedWithTimes.isEmpty()) {
            double avgHours = completedWithTimes.stream()
                    .mapToLong(i -> ChronoUnit.MINUTES.between(i.getOccurredAt(), i.getCompletedAt()))
                    .average()
                    .orElse(0.0) / 60.0;
            upsertStat(date, "INCIDENT", "avg_resolution_hours",
                    BigDecimal.valueOf(avgHours).setScale(2, RoundingMode.HALF_UP), null);
        } else {
            upsertStat(date, "INCIDENT", "avg_resolution_hours", BigDecimal.ZERO, null);
        }

        // sla_compliance_rate
        List<Incident> withSla = completedWithTimes.stream()
                .filter(i -> i.getSlaDeadlineAt() != null)
                .toList();
        if (!withSla.isEmpty()) {
            long compliant = withSla.stream()
                    .filter(i -> !i.getCompletedAt().isAfter(i.getSlaDeadlineAt()))
                    .count();
            BigDecimal rate = BigDecimal.valueOf(compliant * 100.0 / withSla.size())
                    .setScale(2, RoundingMode.HALF_UP);
            upsertStat(date, "INCIDENT", "sla_compliance_rate", rate, null);
        } else {
            upsertStat(date, "INCIDENT", "sla_compliance_rate", BigDecimal.ZERO, null);
        }

        // critical_count & high_count
        long criticalCount = completedIncidents.stream()
                .filter(i -> "CRITICAL".equals(i.getPriorityCd()))
                .count();
        upsertStat(date, "INCIDENT", "critical_count", new BigDecimal(criticalCount), null);

        long highCount = completedIncidents.stream()
                .filter(i -> "HIGH".equals(i.getPriorityCd()))
                .count();
        upsertStat(date, "INCIDENT", "high_count", new BigDecimal(highCount), null);

        // open_count (RECEIVED + IN_PROGRESS)
        List<Incident> openIncidents = incidentRepository.findByStatusCdIn(
                List.of("RECEIVED", "IN_PROGRESS"));
        upsertStat(date, "INCIDENT", "open_count", new BigDecimal(openIncidents.size()), null);

        log.info("[통계] 장애 통계 집계 완료 - 신규: {}, 완료: {}, 미해결: {}",
                totalCount, completedCount, openIncidents.size());
    }

    private void aggregateServiceRequestStats(LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        // total_count
        long totalCount = serviceRequestRepository.countByCreatedAtBetween(dayStart, dayEnd);
        upsertStat(date, "SERVICE_REQUEST", "total_count", new BigDecimal(totalCount), null);

        // completed_count
        List<ServiceRequest> closedSrs = serviceRequestRepository.findByStatusCd("CLOSED");
        long completedCount = closedSrs.stream()
                .filter(sr -> sr.getClosedAt() != null
                        && !sr.getClosedAt().isBefore(dayStart)
                        && sr.getClosedAt().isBefore(dayEnd))
                .count();
        upsertStat(date, "SERVICE_REQUEST", "completed_count", new BigDecimal(completedCount), null);

        // avg_satisfaction
        List<ServiceRequest> withSatisfaction = closedSrs.stream()
                .filter(sr -> sr.getSatisfactionScore() != null)
                .toList();
        if (!withSatisfaction.isEmpty()) {
            double avgSatisfaction = withSatisfaction.stream()
                    .mapToInt(ServiceRequest::getSatisfactionScore)
                    .average()
                    .orElse(0.0);
            upsertStat(date, "SERVICE_REQUEST", "avg_satisfaction",
                    BigDecimal.valueOf(avgSatisfaction).setScale(2, RoundingMode.HALF_UP), null);
        } else {
            upsertStat(date, "SERVICE_REQUEST", "avg_satisfaction", BigDecimal.ZERO, null);
        }

        // open_count (RECEIVED + ASSIGNED + IN_PROGRESS)
        long openReceived = serviceRequestRepository.countByStatusCd("RECEIVED");
        long openAssigned = serviceRequestRepository.countByStatusCd("ASSIGNED");
        long openInProgress = serviceRequestRepository.countByStatusCd("IN_PROGRESS");
        long openCount = openReceived + openAssigned + openInProgress;
        upsertStat(date, "SERVICE_REQUEST", "open_count", new BigDecimal(openCount), null);

        log.info("[통계] 서비스요청 통계 집계 완료 - 신규: {}, 완료: {}, 미처리: {}",
                totalCount, completedCount, openCount);
    }

    private void aggregateChangeStats(LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        // total_count
        long totalCount = changeRepository.countByCreatedAtBetween(dayStart, dayEnd);
        upsertStat(date, "CHANGE", "total_count", new BigDecimal(totalCount), null);

        // completed_count
        long completedCount = changeRepository.countByStatusCd("COMPLETED");
        upsertStat(date, "CHANGE", "completed_count", new BigDecimal(completedCount), null);

        // rejected_count
        long rejectedCount = changeRepository.countByStatusCd("REJECTED");
        upsertStat(date, "CHANGE", "rejected_count", new BigDecimal(rejectedCount), null);

        // approval_rate
        long approvedCount = changeRepository.countByStatusCd("APPROVED");
        long requestedCount = changeRepository.countByStatusCd("APPROVAL_REQUESTED");
        long totalDecisions = approvedCount + rejectedCount;
        if (totalDecisions > 0) {
            BigDecimal rate = BigDecimal.valueOf(approvedCount * 100.0 / totalDecisions)
                    .setScale(2, RoundingMode.HALF_UP);
            upsertStat(date, "CHANGE", "approval_rate", rate, null);
        } else {
            upsertStat(date, "CHANGE", "approval_rate", BigDecimal.ZERO, null);
        }

        log.info("[통계] 변경 통계 집계 완료 - 신규: {}, 완료: {}, 반려: {}",
                totalCount, completedCount, rejectedCount);
    }

    private void aggregateAssetStats(LocalDate date) {
        // hw_total
        long hwTotal = assetHwRepository.count();
        upsertStat(date, "ASSET", "hw_total", new BigDecimal(hwTotal), null);

        // sw_total
        long swTotal = assetSwRepository.count();
        upsertStat(date, "ASSET", "sw_total", new BigDecimal(swTotal), null);

        // hw_active
        List<Object[]> hwByStatus = assetHwRepository.countByStatus();
        long hwActive = hwByStatus.stream()
                .filter(row -> "ACTIVE".equals(row[0]))
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();
        upsertStat(date, "ASSET", "hw_active", new BigDecimal(hwActive), null);

        // sw_active
        List<Object[]> swByStatus = assetSwRepository.countByStatus();
        long swActive = swByStatus.stream()
                .filter(row -> "ACTIVE".equals(row[0]))
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();
        upsertStat(date, "ASSET", "sw_active", new BigDecimal(swActive), null);

        log.info("[통계] 자산 통계 집계 완료 - HW: {} (활성: {}), SW: {} (활성: {})",
                hwTotal, hwActive, swTotal, swActive);
    }

    private void aggregateUserStats(LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        // login_count
        long loginCount = loginHistoryRepository.countByLoginAtBetween(dayStart, dayEnd);
        upsertStat(date, "USER", "login_count", new BigDecimal(loginCount), null);

        // page_view_count
        long pageViewCount = menuAccessLogRepository.countByAccessedAtBetween(dayStart, dayEnd);
        upsertStat(date, "USER", "page_view_count", new BigDecimal(pageViewCount), null);

        // active_user_count (distinct users who logged in)
        List<LoginHistory> loginHistories = loginHistoryRepository.findByLoginAtBetween(dayStart, dayEnd);
        long activeUserCount = loginHistories.stream()
                .map(LoginHistory::getUserId)
                .distinct()
                .count();
        upsertStat(date, "USER", "active_user_count", new BigDecimal(activeUserCount), null);

        log.info("[통계] 사용자 통계 집계 완료 - 로그인: {}, 페이지뷰: {}, 활성유저: {}",
                loginCount, pageViewCount, activeUserCount);
    }

    private void upsertStat(LocalDate date, String statType, String statKey,
                             BigDecimal value, String detail) {
        Optional<DailyStatistics> existing =
                dailyStatisticsRepository.findByStatDateAndStatTypeAndStatKey(date, statType, statKey);

        if (existing.isPresent()) {
            DailyStatistics stat = existing.get();
            stat.updateValue(value, detail);
            dailyStatisticsRepository.save(stat);
        } else {
            DailyStatistics stat = DailyStatistics.builder()
                    .statDate(date)
                    .statType(statType)
                    .statKey(statKey)
                    .statValue(value)
                    .statDetail(detail)
                    .build();
            dailyStatisticsRepository.save(stat);
        }
    }
}
