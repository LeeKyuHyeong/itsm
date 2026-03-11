package com.itsm.api.service.common;

import com.itsm.api.dto.common.NotificationResponse;
import com.itsm.core.domain.common.Notification;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.common.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification notification;
    private Notification unreadNotification;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .userId(1L)
                .notiTypeCd("TICKET")
                .title("티켓이 할당되었습니다")
                .content("티켓 #100이 할당되었습니다.")
                .refType("TICKET")
                .refId(100L)
                .build();
        ReflectionTestUtils.setField(notification, "notiId", 1L);
        ReflectionTestUtils.setField(notification, "createdAt", LocalDateTime.now());

        unreadNotification = Notification.builder()
                .userId(1L)
                .notiTypeCd("APPROVAL")
                .title("승인 요청이 도착했습니다")
                .content("변경 요청 #200에 대한 승인이 필요합니다.")
                .refType("CHANGE")
                .refId(200L)
                .build();
        ReflectionTestUtils.setField(unreadNotification, "notiId", 2L);
        ReflectionTestUtils.setField(unreadNotification, "createdAt", LocalDateTime.now());
    }

    @Test
    @DisplayName("내 알림 목록을 조회한다")
    void getMyNotifications_returnsList() {
        // given
        given(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .willReturn(List.of(notification, unreadNotification));

        // when
        List<NotificationResponse> result = notificationService.getMyNotifications(1L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNotiId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("티켓이 할당되었습니다");
        assertThat(result.get(1).getNotiId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("읽지 않은 알림 수를 조회한다")
    void getUnreadCount_returnsCount() {
        // given
        given(notificationRepository.countByUserIdAndReadAtIsNull(1L)).willReturn(5L);

        // when
        long count = notificationService.getUnreadCount(1L);

        // then
        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("알림을 읽음 처리한다")
    void markAsRead_success() {
        // given
        given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

        // when
        notificationService.markAsRead(1L, 1L);

        // then
        assertThat(notification.getReadAt()).isNotNull();
    }

    @Test
    @DisplayName("다른 사용자의 알림을 읽음 처리하면 ACCESS_DENIED 예외가 발생한다")
    void markAsRead_wrongUserId_throwsAccessDenied() {
        // given
        given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

        // when & then
        assertThatThrownBy(() -> notificationService.markAsRead(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("모든 알림을 읽음 처리한다")
    void markAllAsRead_updatesAllUnread() {
        // given
        given(notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(1L))
                .willReturn(List.of(notification, unreadNotification));

        // when
        notificationService.markAllAsRead(1L);

        // then
        assertThat(notification.getReadAt()).isNotNull();
        assertThat(unreadNotification.getReadAt()).isNotNull();
    }

    @Test
    @DisplayName("알림을 생성한다")
    void sendNotification_createsNotification() {
        // given
        given(notificationRepository.save(any(Notification.class))).willAnswer(invocation -> {
            Notification saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "notiId", 3L);
            return saved;
        });

        // when
        notificationService.sendNotification(1L, "TICKET", "새 티켓", "티켓이 생성되었습니다.", "TICKET", 300L);

        // then
        verify(notificationRepository).save(any(Notification.class));
    }
}
