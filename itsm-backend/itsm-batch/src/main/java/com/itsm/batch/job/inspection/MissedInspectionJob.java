package com.itsm.batch.job.inspection;

import com.itsm.batch.service.NotificationService;
import com.itsm.core.domain.inspection.Inspection;
import com.itsm.core.repository.inspection.InspectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MissedInspectionJob {

    private static final Long ADMIN_USER_ID = 1L;

    private final InspectionRepository inspectionRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 30 8 * * *")
    public void execute() {
        log.info("[MissedInspectionJob] 시작");
        LocalDate today = LocalDate.now();

        List<Inspection> missedInspections = inspectionRepository
                .findByStatusCdAndScheduledAtBefore("SCHEDULED", today);

        int count = 0;
        for (Inspection inspection : missedInspections) {
            Long targetUserId = inspection.getManagerId() != null
                    ? inspection.getManagerId() : ADMIN_USER_ID;

            notificationService.sendNotification(
                    targetUserId,
                    "MISSED_INSPECTION",
                    String.format("[미실시 점검] %s - 예정일 %s 경과",
                            inspection.getTitle(), inspection.getScheduledAt()),
                    String.format("점검 '%s'이(가) 예정일(%s)이 지났으나 아직 실시되지 않았습니다.",
                            inspection.getTitle(), inspection.getScheduledAt()),
                    "INSPECTION",
                    inspection.getInspectionId()
            );
            count++;
        }
        log.info("[MissedInspectionJob] 완료 - {}건 알림 발송", count);
    }
}
