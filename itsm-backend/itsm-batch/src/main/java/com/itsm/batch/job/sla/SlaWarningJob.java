package com.itsm.batch.job.sla;

import com.itsm.batch.service.NotificationService;
import com.itsm.core.domain.incident.Incident;
import com.itsm.core.repository.incident.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlaWarningJob {

    private final IncidentRepository incidentRepository;
    private final NotificationService notificationService;
    private final com.itsm.core.repository.common.SlaPolicyRepository slaPolicyRepository;

    /** 정책이 없을 때의 경고 임계값 (tb_sla_policy.warning_pct 기본 80 과 동일) */
    private static final double DEFAULT_WARNING_RATE = 0.8;

    @Transactional
    public void execute() {
        log.info("[SlaWarningJob] 시작");
        LocalDateTime now = LocalDateTime.now();

        List<Incident> incidents = incidentRepository.findByStatusCdIn(
                List.of("RECEIVED", "IN_PROGRESS"));

        int count = 0;
        for (Incident incident : incidents) {
            if (incident.getSlaDeadlineAt() == null || incident.getOccurredAt() == null) {
                continue;
            }
            if (incident.getMainManager() == null) {
                continue;
            }

            double elapsedRate = calculateElapsedRate(incident.getOccurredAt(),
                    incident.getSlaDeadlineAt(), now);

            // 2026-09-16 P3: 관리자 SLA 화면의 warning_pct 를 읽는다 (회사별 → 전사 기본 → 0.8)
            double warningRate = resolveWarningRate(incident);
            if (elapsedRate >= warningRate && elapsedRate < 1.0) {
                notificationService.sendNotification(
                        incident.getMainManager().getUserId(),
                        "SLA_WARNING",
                        String.format("[SLA 경고] 장애 #%d SLA 경과율 %.0f%%",
                                incident.getIncidentId(), elapsedRate * 100),
                        String.format("장애 '%s'의 SLA 경과율이 %.0f%%입니다. 기한: %s",
                                incident.getTitle(), elapsedRate * 100, incident.getSlaDeadlineAt()),
                        "INCIDENT",
                        incident.getIncidentId()
                );
                count++;
            }
        }
        log.info("[SlaWarningJob] 완료 - {}건 알림 발송", count);
    }

    double resolveWarningRate(Incident incident) {
        Long companyId = incident.getCompany() != null ? incident.getCompany().getCompanyId() : null;
        String priorityCd = incident.getPriorityCd();
        return slaPolicyRepository.findByCompanyIdAndPriorityCd(companyId, priorityCd)
                .or(() -> slaPolicyRepository.findByCompanyIdIsNullAndPriorityCd(priorityCd))
                .map(policy -> policy.getWarningPct() != null ? policy.getWarningPct() / 100.0 : DEFAULT_WARNING_RATE)
                .orElse(DEFAULT_WARNING_RATE);
    }

    double calculateElapsedRate(LocalDateTime occurredAt, LocalDateTime deadlineAt,
                                        LocalDateTime now) {
        long totalMinutes = Duration.between(occurredAt, deadlineAt).toMinutes();
        if (totalMinutes <= 0) {
            return 1.0;
        }
        long elapsedMinutes = Duration.between(occurredAt, now).toMinutes();
        return (double) elapsedMinutes / totalMinutes;
    }
}
