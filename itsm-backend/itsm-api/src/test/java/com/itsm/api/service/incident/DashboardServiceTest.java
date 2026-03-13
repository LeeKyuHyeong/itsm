package com.itsm.api.service.incident;

import com.itsm.api.dto.incident.DashboardStatsResponse;
import com.itsm.api.dto.incident.MonthlyTrendItem;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.incident.Incident;
import com.itsm.core.repository.change.ChangeRepository;
import com.itsm.core.repository.incident.IncidentRepository;
import com.itsm.core.repository.inspection.InspectionRepository;
import com.itsm.core.repository.servicerequest.ServiceRequestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @Mock
    private ChangeRepository changeRepository;

    @Mock
    private InspectionRepository inspectionRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("getStats - 인시던트 상태별 건수를 반환한다")
    void getStats_returnsIncidentStatusCounts() {
        // given
        given(incidentRepository.countByStatusCd("RECEIVED")).willReturn(5L);
        given(incidentRepository.countByStatusCd("IN_PROGRESS")).willReturn(3L);
        given(incidentRepository.countByStatusCd("COMPLETED")).willReturn(10L);
        given(incidentRepository.countByStatusCd("CLOSED")).willReturn(20L);
        given(incidentRepository.countByStatusCd("REJECTED")).willReturn(2L);
        given(incidentRepository.countByStatusCdAndPriorityCd(anyString(), anyString())).willReturn(0L);
        given(incidentRepository.findByStatusCd(anyString())).willReturn(Collections.emptyList());
        given(incidentRepository.findAll(any(Pageable.class))).willReturn(new PageImpl<>(Collections.emptyList()));
        given(incidentRepository.countUnassigned()).willReturn(0L);
        given(incidentRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(serviceRequestRepository.countByStatusCd(anyString())).willReturn(0L);
        given(serviceRequestRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(changeRepository.countByStatusCd(anyString())).willReturn(0L);
        given(changeRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(inspectionRepository.countByStatusCd(anyString())).willReturn(0L);

        // when
        DashboardStatsResponse result = dashboardService.getStats();

        // then
        assertThat(result.getStatusCounts()).containsEntry("RECEIVED", 5L);
        assertThat(result.getStatusCounts()).containsEntry("IN_PROGRESS", 3L);
        assertThat(result.getStatusCounts()).containsEntry("COMPLETED", 10L);
        assertThat(result.getStatusCounts()).containsEntry("CLOSED", 20L);
        assertThat(result.getStatusCounts()).containsEntry("REJECTED", 2L);
        assertThat(result.getTotalIncidentCount()).isEqualTo(40L);
    }

    @Test
    @DisplayName("getStats - SR 상태별 건수를 반환한다")
    void getStats_returnsSrStatusCounts() {
        // given
        stubAllIncidentDefaults();
        given(serviceRequestRepository.countByStatusCd("RECEIVED")).willReturn(3L);
        given(serviceRequestRepository.countByStatusCd("ASSIGNED")).willReturn(2L);
        given(serviceRequestRepository.countByStatusCd("IN_PROGRESS")).willReturn(4L);
        given(serviceRequestRepository.countByStatusCd("PENDING_COMPLETE")).willReturn(1L);
        given(serviceRequestRepository.countByStatusCd("CLOSED")).willReturn(10L);
        given(serviceRequestRepository.countByStatusCd("CANCELLED")).willReturn(0L);
        given(serviceRequestRepository.countByStatusCd("REJECTED")).willReturn(1L);
        given(serviceRequestRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(changeRepository.countByStatusCd(anyString())).willReturn(0L);
        given(changeRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(inspectionRepository.countByStatusCd(anyString())).willReturn(0L);

        // when
        DashboardStatsResponse result = dashboardService.getStats();

        // then
        assertThat(result.getSrStatusCounts()).containsEntry("RECEIVED", 3L);
        assertThat(result.getSrStatusCounts()).containsEntry("IN_PROGRESS", 4L);
        assertThat(result.getSrStatusCounts()).containsEntry("PENDING_COMPLETE", 1L);
        assertThat(result.getTotalSrCount()).isEqualTo(21L);
        assertThat(result.getPendingSrCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getStats - 변경관리 상태별 건수를 반환한다")
    void getStats_returnsChangeStatusCounts() {
        // given
        stubAllIncidentDefaults();
        given(serviceRequestRepository.countByStatusCd(anyString())).willReturn(0L);
        given(serviceRequestRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(changeRepository.countByStatusCd("DRAFT")).willReturn(2L);
        given(changeRepository.countByStatusCd("APPROVAL_REQUESTED")).willReturn(1L);
        given(changeRepository.countByStatusCd("APPROVED")).willReturn(3L);
        given(changeRepository.countByStatusCd("IN_PROGRESS")).willReturn(1L);
        given(changeRepository.countByStatusCd("COMPLETED")).willReturn(5L);
        given(changeRepository.countByStatusCd("CLOSED")).willReturn(8L);
        given(changeRepository.countByStatusCd("REJECTED")).willReturn(0L);
        given(changeRepository.countByStatusCd("CANCELLED")).willReturn(1L);
        given(changeRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(inspectionRepository.countByStatusCd(anyString())).willReturn(0L);

        // when
        DashboardStatsResponse result = dashboardService.getStats();

        // then
        assertThat(result.getChangeStatusCounts()).containsEntry("DRAFT", 2L);
        assertThat(result.getChangeStatusCounts()).containsEntry("APPROVED", 3L);
        assertThat(result.getTotalChangeCount()).isEqualTo(21L);
    }

    @Test
    @DisplayName("getStats - 점검 상태별 건수를 반환한다")
    void getStats_returnsInspectionStatusCounts() {
        // given
        stubAllIncidentDefaults();
        given(serviceRequestRepository.countByStatusCd(anyString())).willReturn(0L);
        given(serviceRequestRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(changeRepository.countByStatusCd(anyString())).willReturn(0L);
        given(changeRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(inspectionRepository.countByStatusCd("SCHEDULED")).willReturn(4L);
        given(inspectionRepository.countByStatusCd("IN_PROGRESS")).willReturn(2L);
        given(inspectionRepository.countByStatusCd("ON_HOLD")).willReturn(1L);
        given(inspectionRepository.countByStatusCd("COMPLETED")).willReturn(6L);
        given(inspectionRepository.countByStatusCd("CLOSED")).willReturn(10L);

        // when
        DashboardStatsResponse result = dashboardService.getStats();

        // then
        assertThat(result.getInspectionStatusCounts()).containsEntry("SCHEDULED", 4L);
        assertThat(result.getInspectionStatusCounts()).containsEntry("IN_PROGRESS", 2L);
        assertThat(result.getTotalInspectionCount()).isEqualTo(23L);
    }

    @Test
    @DisplayName("getStats - 모니터링 지표를 반환한다")
    void getStats_returnsMonitoringMetrics() {
        // given
        given(incidentRepository.countByStatusCd(anyString())).willReturn(0L);
        given(incidentRepository.countByStatusCdAndPriorityCd(anyString(), anyString())).willReturn(0L);
        given(incidentRepository.findByStatusCd(anyString())).willReturn(Collections.emptyList());
        given(incidentRepository.findAll(any(Pageable.class))).willReturn(new PageImpl<>(Collections.emptyList()));
        given(incidentRepository.countUnassigned()).willReturn(7L);
        given(incidentRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(serviceRequestRepository.countByStatusCd(anyString())).willReturn(0L);
        given(serviceRequestRepository.countByStatusCd("PENDING_COMPLETE")).willReturn(3L);
        given(serviceRequestRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(changeRepository.countByStatusCd(anyString())).willReturn(0L);
        given(changeRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(inspectionRepository.countByStatusCd(anyString())).willReturn(0L);

        // when
        DashboardStatsResponse result = dashboardService.getStats();

        // then
        assertThat(result.getUnassignedIncidentCount()).isEqualTo(7L);
        assertThat(result.getPendingSrCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("getStats - SLA 초과 건수를 delayedIncidentCount로도 반환한다")
    void getStats_returnsDelayedIncidentCount() {
        // given
        Company company = Company.builder().companyNm("테스트사").build();
        Incident overdueIncident = Incident.builder()
                .title("SLA 초과 장애")
                .content("내용")
                .incidentTypeCd("SYSTEM")
                .priorityCd("HIGH")
                .occurredAt(LocalDateTime.now().minusDays(5))
                .company(company)
                .build();
        ReflectionTestUtils.setField(overdueIncident, "slaDeadlineAt", LocalDateTime.now().minusHours(1));

        given(incidentRepository.countByStatusCd(anyString())).willReturn(0L);
        given(incidentRepository.countByStatusCdAndPriorityCd(anyString(), anyString())).willReturn(0L);
        given(incidentRepository.findByStatusCd("RECEIVED")).willReturn(List.of(overdueIncident));
        given(incidentRepository.findByStatusCd("IN_PROGRESS")).willReturn(Collections.emptyList());
        given(incidentRepository.findAll(any(Pageable.class))).willReturn(new PageImpl<>(Collections.emptyList()));
        given(incidentRepository.countUnassigned()).willReturn(0L);
        given(incidentRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(serviceRequestRepository.countByStatusCd(anyString())).willReturn(0L);
        given(serviceRequestRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(changeRepository.countByStatusCd(anyString())).willReturn(0L);
        given(changeRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(inspectionRepository.countByStatusCd(anyString())).willReturn(0L);

        // when
        DashboardStatsResponse result = dashboardService.getStats();

        // then
        assertThat(result.getSlaOverdueCount()).isEqualTo(1L);
        assertThat(result.getDelayedIncidentCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getStats - 최근 6개월 월별 추이를 반환한다")
    void getStats_returnsMonthlyTrend() {
        // given
        stubAllIncidentDefaults();
        given(serviceRequestRepository.countByStatusCd(anyString())).willReturn(0L);
        given(changeRepository.countByStatusCd(anyString())).willReturn(0L);
        given(inspectionRepository.countByStatusCd(anyString())).willReturn(0L);

        // Mock monthly counts with specific values for the most recent month
        given(incidentRepository.countByCreatedAtBetween(any(), any())).willReturn(5L);
        given(serviceRequestRepository.countByCreatedAtBetween(any(), any())).willReturn(3L);
        given(changeRepository.countByCreatedAtBetween(any(), any())).willReturn(2L);

        // when
        DashboardStatsResponse result = dashboardService.getStats();

        // then
        assertThat(result.getMonthlyTrend()).hasSize(6);
        MonthlyTrendItem latest = result.getMonthlyTrend().get(5);
        assertThat(latest.getMonth()).isNotNull();
        assertThat(latest.getIncidentCount()).isEqualTo(5L);
        assertThat(latest.getSrCount()).isEqualTo(3L);
        assertThat(latest.getChangeCount()).isEqualTo(2L);
    }

    private void stubAllIncidentDefaults() {
        given(incidentRepository.countByStatusCd(anyString())).willReturn(0L);
        given(incidentRepository.countByStatusCdAndPriorityCd(anyString(), anyString())).willReturn(0L);
        given(incidentRepository.findByStatusCd(anyString())).willReturn(Collections.emptyList());
        given(incidentRepository.findAll(any(Pageable.class))).willReturn(new PageImpl<>(Collections.emptyList()));
        given(incidentRepository.countUnassigned()).willReturn(0L);
        given(incidentRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
    }
}
