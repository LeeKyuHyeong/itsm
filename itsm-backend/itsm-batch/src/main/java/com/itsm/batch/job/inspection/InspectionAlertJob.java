package com.itsm.batch.job.inspection;

import com.itsm.batch.service.NotificationService;
import com.itsm.core.domain.inspection.Inspection;
import com.itsm.core.repository.inspection.InspectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InspectionAlertJob {

    private static final Long ADMIN_USER_ID = 1L;

    private final InspectionRepository inspectionRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 7 * * *")
    public void execute() {
        log.info("[InspectionAlertJob] 시작");
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysLater = today.plusDays(7);

        List<Inspection> upcomingInspections = inspectionRepository
                .findByStatusCdAndScheduledAtBetween("SCHEDULED", today, sevenDaysLater);

        int count = 0;
        for (Inspection inspection : upcomingInspections) {
            long daysUntil = ChronoUnit.DAYS.between(today, inspection.getScheduledAt());
            Long targetUserId = inspection.getManagerId() != null
                    ? inspection.getManagerId() : ADMIN_USER_ID;

            notificationService.sendNotification(
                    targetUserId,
                    "INSPECTION_UPCOMING",
                    String.format("[점검 임박] %s - %d일 후 예정", inspection.getTitle(), daysUntil),
                    String.format("점검 '%s'이(가) %s에 예정되어 있습니다. (잔여 %d일)",
                            inspection.getTitle(), inspection.getScheduledAt(), daysUntil),
                    "INSPECTION",
                    inspection.getInspectionId()
            );
            count++;
        }
        log.info("[InspectionAlertJob] 완료 - {}건 알림 발송", count);
    }
}
