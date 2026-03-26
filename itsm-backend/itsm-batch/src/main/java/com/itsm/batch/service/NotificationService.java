package com.itsm.batch.service;

import com.itsm.core.domain.common.Notification;
import com.itsm.core.repository.common.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private static final Set<String> ALLOWED_REF_TYPES = Set.of(
            "INCIDENT", "SERVICE_REQUEST", "CHANGE",
            "INSPECTION", "ASSET_HW", "ASSET_SW", "ASSET_OA", "SIMULATION"
    );

    private final NotificationRepository notificationRepository;

    @Transactional
    public void sendNotification(Long userId, String notiTypeCd, String title,
                                  String content, String refType, Long refId) {
        if (refType != null && !ALLOWED_REF_TYPES.contains(refType)) {
            log.warn("[Batch Notification] 허용되지 않은 refType 무시: {}", refType);
            refType = null;
            refId = null;
        }

        Notification notification = Notification.builder()
                .userId(userId)
                .notiTypeCd(notiTypeCd)
                .title(title)
                .content(content)
                .refType(refType)
                .refId(refId)
                .build();
        notificationRepository.save(notification);
        log.info("[Batch Notification] type={}, userId={}, refType={}, refId={}",
                notiTypeCd, userId, refType, refId);
    }
}
