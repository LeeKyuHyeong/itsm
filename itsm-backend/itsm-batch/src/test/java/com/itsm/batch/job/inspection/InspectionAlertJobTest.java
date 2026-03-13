package com.itsm.batch.job.inspection;

import com.itsm.batch.service.NotificationService;
import com.itsm.core.domain.inspection.Inspection;
import com.itsm.core.repository.inspection.InspectionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InspectionAlertJobTest {

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private InspectionAlertJob inspectionAlertJob;

    @Test
    @DisplayName("점검 예정일이 7일 이내인 점검에 대해 알림을 발송한다")
    void execute_sendsAlertForUpcomingInspections() {
        // given
        Inspection inspection = mock(Inspection.class);
        when(inspection.getInspectionId()).thenReturn(1L);
        when(inspection.getTitle()).thenReturn("서버실 점검");
        when(inspection.getScheduledAt()).thenReturn(LocalDate.now().plusDays(3));
        when(inspection.getManagerId()).thenReturn(10L);

        when(inspectionRepository.findByStatusCdAndScheduledAtBetween(
                eq("SCHEDULED"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(inspection));

        // when
        inspectionAlertJob.execute();

        // then
        verify(notificationService).sendNotification(
                eq(10L),
                eq("INSPECTION_UPCOMING"),
                contains("점검 임박"),
                anyString(),
                eq("INSPECTION"),
                eq(1L)
        );
    }

    @Test
    @DisplayName("담당자가 없는 점검은 관리자에게 알림을 발송한다")
    void execute_sendsAlertToAdminWhenNoManager() {
        // given
        Inspection inspection = mock(Inspection.class);
        when(inspection.getInspectionId()).thenReturn(2L);
        when(inspection.getTitle()).thenReturn("네트워크 점검");
        when(inspection.getScheduledAt()).thenReturn(LocalDate.now().plusDays(5));
        when(inspection.getManagerId()).thenReturn(null);

        when(inspectionRepository.findByStatusCdAndScheduledAtBetween(
                eq("SCHEDULED"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(inspection));

        // when
        inspectionAlertJob.execute();

        // then
        verify(notificationService).sendNotification(
                eq(1L),
                eq("INSPECTION_UPCOMING"),
                anyString(),
                anyString(),
                eq("INSPECTION"),
                eq(2L)
        );
    }

    @Test
    @DisplayName("예정 점검이 없으면 알림을 발송하지 않는다")
    void execute_doesNotSendWhenNoUpcomingInspections() {
        // given
        when(inspectionRepository.findByStatusCdAndScheduledAtBetween(
                eq("SCHEDULED"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // when
        inspectionAlertJob.execute();

        // then
        verify(notificationService, never()).sendNotification(
                anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong());
    }
}
