package com.itsm.batch.job.incident;

import com.itsm.batch.service.NotificationService;
import com.itsm.core.domain.incident.Incident;
import com.itsm.core.repository.incident.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UnassignedIncidentJob {

    private static final Long ADMIN_USER_ID = 1L;

    private final IncidentRepository incidentRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 20 * * * *")
    public void execute() {
        log.info("[UnassignedIncidentJob] 시작");

        List<Incident> incidents = incidentRepository.findByStatusCdInAndMainManagerIsNull(
                List.of("RECEIVED"));

        for (Incident incident : incidents) {
            notificationService.sendNotification(
                    ADMIN_USER_ID,
                    "UNASSIGNED_INCIDENT",
                    String.format("[미배정 장애] 장애 #%d 담당자 미배정", incident.getIncidentId()),
                    String.format("장애 '%s'에 담당자가 배정되지 않았습니다.", incident.getTitle()),
                    "INCIDENT",
                    incident.getIncidentId()
            );
        }
        log.info("[UnassignedIncidentJob] 완료 - {}건 알림 발송", incidents.size());
    }
}
