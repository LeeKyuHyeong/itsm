package com.itsm.batch.job.sla;

import com.itsm.batch.service.NotificationService;
import com.itsm.core.domain.incident.Incident;
import com.itsm.core.domain.user.User;
import com.itsm.core.repository.incident.IncidentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlaWarningJobTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SlaWarningJob slaWarningJob;

    @Test
    @DisplayName("SLA 경과율 80% 이상인 장애에 대해 경고 알림을 발송한다")
    void execute_sendsWarningForHighElapsedRate() {
        // given
        User manager = mock(User.class);
        when(manager.getUserId()).thenReturn(10L);

        Incident incident = mock(Incident.class);
        when(incident.getIncidentId()).thenReturn(1L);
        when(incident.getTitle()).thenReturn("서버 장애");
        when(incident.getMainManager()).thenReturn(manager);
        // SLA: occurred 10 hours ago, deadline 2 hours from now => 83% elapsed
        when(incident.getOccurredAt()).thenReturn(LocalDateTime.now().minusHours(10));
        when(incident.getSlaDeadlineAt()).thenReturn(LocalDateTime.now().plusHours(2));

        when(incidentRepository.findByStatusCdIn(List.of("RECEIVED", "IN_PROGRESS")))
                .thenReturn(List.of(incident));

        // when
        slaWarningJob.execute();

        // then
        verify(notificationService).sendNotification(
                eq(10L),
                eq("SLA_WARNING"),
                contains("SLA 경고"),
                anyString(),
                eq("INCIDENT"),
                eq(1L)
        );
    }

    @Test
    @DisplayName("SLA 경과율 80% 미만인 장애에 대해서는 알림을 발송하지 않는다")
    void execute_doesNotSendWarningForLowElapsedRate() {
        // given
        User manager = mock(User.class);

        Incident incident = mock(Incident.class);
        when(incident.getMainManager()).thenReturn(manager);
        // SLA: occurred 2 hours ago, deadline 10 hours from now => ~17% elapsed
        when(incident.getOccurredAt()).thenReturn(LocalDateTime.now().minusHours(2));
        when(incident.getSlaDeadlineAt()).thenReturn(LocalDateTime.now().plusHours(10));

        when(incidentRepository.findByStatusCdIn(List.of("RECEIVED", "IN_PROGRESS")))
                .thenReturn(List.of(incident));

        // when
        slaWarningJob.execute();

        // then
        verify(notificationService, never()).sendNotification(
                anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("SLA 기한이 이미 초과된 장애에 대해서는 경고 알림을 발송하지 않는다")
    void execute_doesNotSendWarningForOverdue() {
        // given
        User manager = mock(User.class);

        Incident incident = mock(Incident.class);
        when(incident.getMainManager()).thenReturn(manager);
        // SLA already overdue
        when(incident.getOccurredAt()).thenReturn(LocalDateTime.now().minusHours(12));
        when(incident.getSlaDeadlineAt()).thenReturn(LocalDateTime.now().minusHours(1));

        when(incidentRepository.findByStatusCdIn(List.of("RECEIVED", "IN_PROGRESS")))
                .thenReturn(List.of(incident));

        // when
        slaWarningJob.execute();

        // then
        verify(notificationService, never()).sendNotification(
                anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("담당자가 없는 장애는 SLA 경고 대상에서 제외된다")
    void execute_skipsIncidentWithoutManager() {
        // given
        Incident incident = mock(Incident.class);
        when(incident.getMainManager()).thenReturn(null);
        when(incident.getSlaDeadlineAt()).thenReturn(LocalDateTime.now().plusHours(1));
        when(incident.getOccurredAt()).thenReturn(LocalDateTime.now().minusHours(10));

        when(incidentRepository.findByStatusCdIn(List.of("RECEIVED", "IN_PROGRESS")))
                .thenReturn(List.of(incident));

        // when
        slaWarningJob.execute();

        // then
        verify(notificationService, never()).sendNotification(
                anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("SLA 경과율 계산 - 정상 케이스")
    void calculateElapsedRate_normalCase() {
        LocalDateTime occurred = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime deadline = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 8, 0);

        double rate = slaWarningJob.calculateElapsedRate(occurred, deadline, now);
        assertThat(rate).isEqualTo(0.8);
    }

    @Test
    @DisplayName("SLA 경과율 계산 - 기한 초과 시 1.0 이상")
    void calculateElapsedRate_overdue() {
        LocalDateTime occurred = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime deadline = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);

        double rate = slaWarningJob.calculateElapsedRate(occurred, deadline, now);
        assertThat(rate).isGreaterThan(1.0);
    }
}
