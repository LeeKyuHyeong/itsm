package com.itsm.batch.job.sla;

import com.itsm.batch.service.NotificationService;
import com.itsm.core.domain.incident.Incident;
import com.itsm.core.repository.incident.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlaOverdueJob {

    private final IncidentRepository incidentRepository;
    private final NotificationService notificationService;

    public void execute() {
        log.info("[SlaOverdueJob] 시작");
        LocalDateTime now = LocalDateTime.now();

        List<Incident> incidents = incidentRepository.findByStatusCdIn(
                List.of("RECEIVED", "IN_PROGRESS"));

        int count = 0;
        for (Incident incident : incidents) {
            if (incident.getSlaDeadlineAt() == null) {
                continue;
            }
            if (incident.getMainManager() == null) {
                continue;
            }

            if (incident.getSlaDeadlineAt().isBefore(now)) {
                notificationService.sendNotification(
                        incident.getMainManager().getUserId(),
                        "SLA_OVERDUE",
                        String.format("[SLA 초과] 장애 #%d SLA 기한 초과", incident.getIncidentId()),
                        String.format("장애 '%s'의 SLA 기한(%s)이 초과되었습니다. 즉시 처리가 필요합니다.",
                                incident.getTitle(), incident.getSlaDeadlineAt()),
                        "INCIDENT",
                        incident.getIncidentId()
                );
                count++;
            }
        }
        log.info("[SlaOverdueJob] 완료 - {}건 알림 발송", count);
    }
}
