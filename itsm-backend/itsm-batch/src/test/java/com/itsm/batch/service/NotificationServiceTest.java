package com.itsm.batch.service;

import com.itsm.core.domain.common.Notification;
import com.itsm.core.repository.common.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private com.itsm.core.repository.common.NotificationPolicyRepository notificationPolicyRepository;

    @InjectMocks
    private NotificationService notificationService;

    // ── 2026-09-16 전수조사 P4: 같은 대상에 대한 같은 유형 알림이 배치가 돌 때마다(매시간) 다시 쌓였다 — 중복 억제 ──

    @Test
    @DisplayName("같은 사용자·유형·대상에 24시간 안에 이미 보낸 알림이 있으면 다시 만들지 않는다")
    void sendNotification_duplicateWithin24h_skips() {
        when(notificationRepository.existsByUserIdAndNotiTypeCdAndRefTypeAndRefIdAndCreatedAtAfter(
                eq(1L), eq("SLA_OVERDUE"), eq("INCIDENT"), eq(100L), any(java.time.LocalDateTime.class)))
                .thenReturn(true);

        notificationService.sendNotification(1L, "SLA_OVERDUE", "t", "c", "INCIDENT", 100L);

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("24시간 안에 같은 알림이 없으면 만든다")
    void sendNotification_noRecentDuplicate_saves() {
        when(notificationRepository.existsByUserIdAndNotiTypeCdAndRefTypeAndRefIdAndCreatedAtAfter(
                eq(1L), eq("SLA_OVERDUE"), eq("INCIDENT"), eq(100L), any(java.time.LocalDateTime.class)))
                .thenReturn(false);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.sendNotification(1L, "SLA_OVERDUE", "t", "c", "INCIDENT", 100L);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("대상(refType/refId)이 없는 알림은 중복 검사 없이 만든다")
    void sendNotification_withoutRef_skipsDedupeCheck() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.sendNotification(2L, "SYSTEM", "t", "c", null, null);

        verify(notificationRepository, never())
                .existsByUserIdAndNotiTypeCdAndRefTypeAndRefIdAndCreatedAtAfter(any(), any(), any(), any(), any());
        verify(notificationRepository).save(any(Notification.class));
    }

    // ── 2026-09-16 전수조사 P3: tb_notification_policy 는 관리자 화면에서 CRUD 만 되고 읽는 코드가 0 이었다 ──

    @Test
    @DisplayName("해당 유형의 알림 정책이 있고 전부 비활성이면 알림을 만들지 않는다")
    void sendNotification_policyInactive_skips() {
        when(notificationPolicyRepository.findByNotiTypeCd("SLA_WARNING"))
                .thenReturn(java.util.List.of(com.itsm.core.domain.common.NotificationPolicy.builder()
                        .notiTypeCd("SLA_WARNING").triggerCondition("elapsed>=warning_pct")
                        .targetRoleCd("MAINTENANCE").isActive("N").build()));

        notificationService.sendNotification(1L, "SLA_WARNING", "t", "c", "INCIDENT", 100L);

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("해당 유형의 알림 정책이 활성이면 알림을 만든다")
    void sendNotification_policyActive_saves() {
        when(notificationPolicyRepository.findByNotiTypeCd("SLA_WARNING"))
                .thenReturn(java.util.List.of(com.itsm.core.domain.common.NotificationPolicy.builder()
                        .notiTypeCd("SLA_WARNING").triggerCondition("x").targetRoleCd("MAINTENANCE").isActive("Y").build()));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.sendNotification(1L, "SLA_WARNING", "t", "c", "INCIDENT", 100L);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("정책 행이 없는 유형은 기존처럼 그대로 만든다 (정책 미설정 = 발송)")
    void sendNotification_noPolicyRows_saves() {
        when(notificationPolicyRepository.findByNotiTypeCd("SLA_OVERDUE")).thenReturn(java.util.List.of());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.sendNotification(1L, "SLA_OVERDUE", "t", "c", "INCIDENT", 100L);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("알림 생성 시 올바른 필드가 설정된다")
    void sendNotification_createsNotificationWithCorrectFields() {
        // given
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        notificationService.sendNotification(1L, "SLA_WARNING", "테스트 제목",
                "테스트 내용", "INCIDENT", 100L);

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getNotiTypeCd()).isEqualTo("SLA_WARNING");
        assertThat(saved.getTitle()).isEqualTo("테스트 제목");
        assertThat(saved.getContent()).isEqualTo("테스트 내용");
        assertThat(saved.getRefType()).isEqualTo("INCIDENT");
        assertThat(saved.getRefId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("알림 생성 시 refType과 refId가 null이어도 정상 동작한다")
    void sendNotification_withNullRefTypeAndRefId() {
        // given
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        notificationService.sendNotification(2L, "SYSTEM", "시스템 알림",
                "시스템 알림 내용", null, null);

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(2L);
        assertThat(saved.getRefType()).isNull();
        assertThat(saved.getRefId()).isNull();
    }

    @Test
    @DisplayName("허용되지 않은 refType은 null로 치환되어 저장된다")
    void sendNotification_invalidRefType_savedWithNullRef() {
        // given
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        notificationService.sendNotification(1L, "TEST", "제목",
                "내용", "MALICIOUS_TYPE", 1L);

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getRefType()).isNull();
        assertThat(saved.getRefId()).isNull();
    }
}
