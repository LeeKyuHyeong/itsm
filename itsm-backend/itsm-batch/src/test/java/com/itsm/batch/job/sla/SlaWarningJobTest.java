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

    @Mock
    private com.itsm.core.repository.common.SlaPolicyRepository slaPolicyRepository;

    @InjectMocks
    private SlaWarningJob slaWarningJob;

    // ── 2026-09-16 전수조사 P3: 관리자 SLA 화면의 warning_pct 를 배치가 읽지 않고 0.8 을 하드코딩하고 있었다 ──

    private Incident incidentAt(double elapsedRate, Long companyId, String priorityCd) {
        User manager = mock(User.class);
        lenient().when(manager.getUserId()).thenReturn(10L); // 경고 미발송 케이스에선 안 읽힘
        com.itsm.core.domain.company.Company company = mock(com.itsm.core.domain.company.Company.class);
        when(company.getCompanyId()).thenReturn(companyId);
        Incident incident = mock(Incident.class);
        lenient().when(incident.getIncidentId()).thenReturn(1L);
        when(incident.getMainManager()).thenReturn(manager);
        when(incident.getCompany()).thenReturn(company);
        when(incident.getPriorityCd()).thenReturn(priorityCd);
        // 총 100분 SLA 중 elapsedRate 만큼 지난 시점
        LocalDateTime occurred = LocalDateTime.now().minusMinutes((long) (elapsedRate * 100));
        when(incident.getOccurredAt()).thenReturn(occurred);
        when(incident.getSlaDeadlineAt()).thenReturn(occurred.plusMinutes(100));
        when(incidentRepository.findByStatusCdIn(List.of("RECEIVED", "IN_PROGRESS")))
                .thenReturn(List.of(incident));
        return incident;
    }

    @Test
    @DisplayName("회사별 SLA 정책 warning_pct=50 이면 경과율 60% 에도 경고를 보낸다")
    void execute_usesCompanyWarningPctFromSlaPolicy() {
        incidentAt(0.6, 7L, "HIGH");
        when(slaPolicyRepository.findByCompanyIdAndPriorityCd(7L, "HIGH"))
                .thenReturn(java.util.Optional.of(com.itsm.core.domain.common.SlaPolicy.builder()
                        .companyId(7L).priorityCd("HIGH").deadlineHours(8).warningPct(50).build()));

        slaWarningJob.execute();

        verify(notificationService).sendNotification(eq(10L), eq("SLA_WARNING"), contains("SLA 경고"),
                anyString(), eq("INCIDENT"), eq(1L));
    }

    @Test
    @DisplayName("회사 정책이 없으면 전사 기본 정책(warning_pct=90)을 쓴다 → 85% 는 경고 아님")
    void execute_fallsBackToGlobalPolicy() {
        incidentAt(0.85, 7L, "HIGH");
        when(slaPolicyRepository.findByCompanyIdAndPriorityCd(7L, "HIGH")).thenReturn(java.util.Optional.empty());
        when(slaPolicyRepository.findByCompanyIdIsNullAndPriorityCd("HIGH"))
                .thenReturn(java.util.Optional.of(com.itsm.core.domain.common.SlaPolicy.builder()
                        .priorityCd("HIGH").deadlineHours(8).warningPct(90).build()));

        slaWarningJob.execute();

        verify(notificationService, never()).sendNotification(
                anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("정책이 아예 없으면 기존 기본값 80% 를 유지한다")
    void execute_defaultsTo80WhenNoPolicy() {
        incidentAt(0.85, 7L, "HIGH");

        slaWarningJob.execute();

        verify(notificationService).sendNotification(eq(10L), eq("SLA_WARNING"), anyString(),
                anyString(), eq("INCIDENT"), eq(1L));
    }

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
