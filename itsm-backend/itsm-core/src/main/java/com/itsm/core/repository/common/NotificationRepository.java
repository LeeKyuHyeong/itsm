package com.itsm.core.repository.common;

import com.itsm.core.domain.common.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndReadAtIsNull(Long userId);

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    /** 배치 중복 발송 억제용 (2026-09-16 P4): 같은 사용자·유형·대상에 최근 보낸 알림이 있는가 */
    boolean existsByUserIdAndNotiTypeCdAndRefTypeAndRefIdAndCreatedAtAfter(
            Long userId, String notiTypeCd, String refType, Long refId, java.time.LocalDateTime after);
}
