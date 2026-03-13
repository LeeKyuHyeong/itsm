package com.itsm.batch.job.sla;

import com.itsm.batch.service.NotificationService;
import com.itsm.core.domain.incident.Incident;
import com.itsm.core.repository.incident.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlaWarningJob {

    private final IncidentRepository incidentRepository;
    private final NotificationService notificationService;

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

            if (elapsedRate >= 0.8 && elapsedRate < 1.0) {
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
