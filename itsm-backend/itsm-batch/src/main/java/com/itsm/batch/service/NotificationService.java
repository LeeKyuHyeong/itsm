package com.itsm.batch.service;

import com.itsm.core.domain.common.Notification;
import com.itsm.core.repository.common.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void sendNotification(Long userId, String notiTypeCd, String title,
                                  String content, String refType, Long refId) {
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
