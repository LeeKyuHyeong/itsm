package com.itsm.batch.job.incident;

import com.itsm.batch.service.NotificationService;
import com.itsm.core.domain.incident.Incident;
import com.itsm.core.repository.incident.IncidentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnassignedIncidentJobTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UnassignedIncidentJob unassignedIncidentJob;

    @Test
    @DisplayName("미배정 장애가 있으면 관리자에게 알림을 발송한다")
    void execute_sendsNotificationForUnassignedIncidents() {
        // given
        Incident incident1 = mock(Incident.class);
        when(incident1.getIncidentId()).thenReturn(1L);
        when(incident1.getTitle()).thenReturn("네트워크 장애");

        Incident incident2 = mock(Incident.class);
        when(incident2.getIncidentId()).thenReturn(2L);
        when(incident2.getTitle()).thenReturn("서버 장애");

        when(incidentRepository.findByStatusCdInAndMainManagerIsNull(List.of("RECEIVED")))
                .thenReturn(List.of(incident1, incident2));

        // when
        unassignedIncidentJob.execute();

        // then
        verify(notificationService, times(2)).sendNotification(
                eq(1L),
                eq("UNASSIGNED_INCIDENT"),
                contains("미배정 장애"),
                anyString(),
                eq("INCIDENT"),
                anyLong()
        );
    }

    @Test
    @DisplayName("미배정 장애가 없으면 알림을 발송하지 않는다")
    void execute_doesNotSendWhenNoUnassigned() {
        // given
        when(incidentRepository.findByStatusCdInAndMainManagerIsNull(List.of("RECEIVED")))
                .thenReturn(Collections.emptyList());

        // when
        unassignedIncidentJob.execute();

        // then
        verify(notificationService, never()).sendNotification(
                anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong());
    }
}
