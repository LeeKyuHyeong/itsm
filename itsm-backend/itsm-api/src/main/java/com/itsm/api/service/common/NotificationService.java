package com.itsm.api.service.common;

import com.itsm.api.dto.common.NotificationResponse;
import com.itsm.core.domain.common.Notification;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.common.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadAtIsNull(userId);
    }

    public void markAsRead(Long notiId, Long userId) {
        Notification notification = notificationRepository.findById(notiId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "알림을 찾을 수 없습니다."));

        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "해당 알림에 대한 접근 권한이 없습니다.");
        }

        notification.markAsRead();
    }

    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications =
                notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId);
        unreadNotifications.forEach(Notification::markAsRead);
    }

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
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .notiId(notification.getNotiId())
                .notiTypeCd(notification.getNotiTypeCd())
                .title(notification.getTitle())
                .content(notification.getContent())
                .refType(notification.getRefType())
                .refId(notification.getRefId())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
