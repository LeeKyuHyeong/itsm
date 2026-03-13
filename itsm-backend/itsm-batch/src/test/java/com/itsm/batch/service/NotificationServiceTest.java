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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

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
}
