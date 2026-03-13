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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlaOverdueJobTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SlaOverdueJob slaOverdueJob;

    @Test
    @DisplayName("SLA 기한이 초과된 장애에 대해 에스컬레이션 알림을 발송한다")
    void execute_sendsOverdueNotification() {
        // given
        User manager = mock(User.class);
        when(manager.getUserId()).thenReturn(5L);

        Incident incident = mock(Incident.class);
        when(incident.getIncidentId()).thenReturn(1L);
        when(incident.getTitle()).thenReturn("DB 장애");
        when(incident.getMainManager()).thenReturn(manager);
        when(incident.getSlaDeadlineAt()).thenReturn(LocalDateTime.now().minusHours(2));

        when(incidentRepository.findByStatusCdIn(List.of("RECEIVED", "IN_PROGRESS")))
                .thenReturn(List.of(incident));

        // when
        slaOverdueJob.execute();

        // then
        verify(notificationService).sendNotification(
                eq(5L),
                eq("SLA_OVERDUE"),
                contains("SLA 초과"),
                anyString(),
                eq("INCIDENT"),
                eq(1L)
        );
    }

    @Test
    @DisplayName("SLA 기한이 아직 남은 장애에 대해서는 알림을 발송하지 않는다")
    void execute_doesNotSendForNonOverdue() {
        // given
        User manager = mock(User.class);

        Incident incident = mock(Incident.class);
        when(incident.getMainManager()).thenReturn(manager);
        when(incident.getSlaDeadlineAt()).thenReturn(LocalDateTime.now().plusHours(5));

        when(incidentRepository.findByStatusCdIn(List.of("RECEIVED", "IN_PROGRESS")))
                .thenReturn(List.of(incident));

        // when
        slaOverdueJob.execute();

        // then
        verify(notificationService, never()).sendNotification(
                anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("SLA 기한이 null인 장애는 건너뛴다")
    void execute_skipsIncidentWithNullSlaDeadline() {
        // given
        Incident incident = mock(Incident.class);
        when(incident.getSlaDeadlineAt()).thenReturn(null);

        when(incidentRepository.findByStatusCdIn(List.of("RECEIVED", "IN_PROGRESS")))
                .thenReturn(List.of(incident));

        // when
        slaOverdueJob.execute();

        // then
        verify(notificationService, never()).sendNotification(
                anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("담당자가 없는 장애는 에스컬레이션 알림 대상에서 제외된다")
    void execute_skipsIncidentWithoutManager() {
        // given
        Incident incident = mock(Incident.class);
        when(incident.getSlaDeadlineAt()).thenReturn(LocalDateTime.now().minusHours(1));
        when(incident.getMainManager()).thenReturn(null);

        when(incidentRepository.findByStatusCdIn(List.of("RECEIVED", "IN_PROGRESS")))
                .thenReturn(List.of(incident));

        // when
        slaOverdueJob.execute();

        // then
        verify(notificationService, never()).sendNotification(
                anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong());
    }
}
