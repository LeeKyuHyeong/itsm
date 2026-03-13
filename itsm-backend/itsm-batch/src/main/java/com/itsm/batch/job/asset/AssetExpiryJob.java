package com.itsm.batch.job.asset;

import com.itsm.batch.service.NotificationService;
import com.itsm.core.domain.asset.AssetHw;
import com.itsm.core.repository.asset.AssetHwRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssetExpiryJob {

    private static final Long ADMIN_USER_ID = 1L;

    private final AssetHwRepository assetHwRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 7 * * *")
    public void execute() {
        log.info("[AssetExpiryJob] 시작");
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);

        List<AssetHw> expiringAssets = assetHwRepository
                .findByWarrantyEndAtBetween(today, thirtyDaysLater);

        int count = 0;
        for (AssetHw asset : expiringAssets) {
            long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(today, asset.getWarrantyEndAt());
            String urgency = daysUntilExpiry <= 7 ? "긴급" : "알림";

            notificationService.sendNotification(
                    ADMIN_USER_ID,
                    "ASSET_EXPIRY",
                    String.format("[자산 만료 %s] %s - 보증 만료 %d일 전",
                            urgency, asset.getAssetNm(), daysUntilExpiry),
                    String.format("HW 자산 '%s'(S/N: %s)의 보증이 %s에 만료됩니다. (잔여 %d일)",
                            asset.getAssetNm(), asset.getSerialNo(),
                            asset.getWarrantyEndAt(), daysUntilExpiry),
                    "ASSET_HW",
                    asset.getAssetHwId()
            );
            count++;
        }
        log.info("[AssetExpiryJob] 완료 - {}건 알림 발송", count);
    }
}
