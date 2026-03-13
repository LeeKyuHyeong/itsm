package com.itsm.batch.job.incident;

import com.itsm.batch.service.NotificationService;
import com.itsm.core.repository.incident.IncidentAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RepeatIncidentJob {

    private static final Long ADMIN_USER_ID = 1L;

    private final IncidentAssetRepository incidentAssetRepository;
    private final NotificationService notificationService;

    public void execute() {
        log.info("[RepeatIncidentJob] 시작");
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        List<Object[]> results = incidentAssetRepository
                .findRepeatIncidentAssets(thirtyDaysAgo, 3L);

        for (Object[] row : results) {
            String assetType = (String) row[0];
            Long assetId = (Long) row[1];
            Long incidentCount = (Long) row[2];

            notificationService.sendNotification(
                    ADMIN_USER_ID,
                    "REPEAT_INCIDENT",
                    String.format("[반복 장애] %s 자산 #%d - 30일간 %d건 발생",
                            assetType, assetId, incidentCount),
                    String.format("자산(유형: %s, ID: %d)에서 최근 30일간 %d건의 장애가 발생하였습니다. 근본 원인 분석이 필요합니다.",
                            assetType, assetId, incidentCount),
                    "ASSET_" + assetType,
                    assetId
            );
        }
        log.info("[RepeatIncidentJob] 완료 - {}건 알림 발송", results.size());
    }
}
