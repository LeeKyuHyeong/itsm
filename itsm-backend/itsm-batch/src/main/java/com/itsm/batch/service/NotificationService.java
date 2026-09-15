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
    private final com.itsm.core.repository.common.NotificationPolicyRepository notificationPolicyRepository;

    @Transactional
    public void sendNotification(Long userId, String notiTypeCd, String title,
                                  String content, String refType, Long refId) {
        // 2026-09-16 P3: 관리자 알림 정책(tb_notification_policy)을 읽는다.
        // 해당 유형의 정책 행이 있는데 전부 비활성이면 발송하지 않는다. 행이 없으면(미설정) 기존대로 발송.
        if (isSuppressedByPolicy(notiTypeCd)) {
            log.info("[Batch Notification] 정책 비활성으로 미발송: type={}, userId={}", notiTypeCd, userId);
            return;
        }
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

    private boolean isSuppressedByPolicy(String notiTypeCd) {
        var policies = notificationPolicyRepository.findByNotiTypeCd(notiTypeCd);
        if (policies == null || policies.isEmpty()) {
            return false;
        }
        return policies.stream().noneMatch(p -> "Y".equals(p.getIsActive()));
    }
}
