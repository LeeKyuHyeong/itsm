package com.itsm.api.service.incident;

import com.itsm.api.dto.incident.DashboardStatsResponse;
import com.itsm.api.dto.incident.IncidentResponse;
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
    @DisplayName("getStats - 인시던트 상태별 건수를 집계 쿼리로 반환한다")
    void getStats_returnsIncidentStatusCounts() {
        // given
        given(incidentRepository.countGroupByStatusCd()).willReturn(List.of(
                new Object[]{"RECEIVED", 5L},
                new Object[]{"IN_PROGRESS", 3L},
                new Object[]{"COMPLETED", 10L},
                new Object[]{"CLOSED", 20L},
                new Object[]{"REJECTED", 2L}
        ));
        given(incidentRepository.countActivePriorityGrouped()).willReturn(Collections.emptyList());
        given(incidentRepository.countSlaOverdue(any())).willReturn(0L);
        given(incidentRepository.countSlaWarning(any())).willReturn(0L);
        given(incidentRepository.findRecentWithCompany(any(Pageable.class))).willReturn(new PageImpl<>(Collections.emptyList()));
        given(incidentRepository.countUnassigned()).willReturn(0L);
        given(incidentRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(serviceRequestRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(serviceRequestRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(changeRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(changeRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(inspectionRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());

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
    @DisplayName("getStats - SR 상태별 건수를 집계 쿼리로 반환한다")
    void getStats_returnsSrStatusCounts() {
        // given
        stubAllIncidentDefaults();
        given(serviceRequestRepository.countGroupByStatusCd()).willReturn(List.of(
                new Object[]{"RECEIVED", 3L},
                new Object[]{"ASSIGNED", 2L},
                new Object[]{"IN_PROGRESS", 4L},
                new Object[]{"PENDING_COMPLETE", 1L},
                new Object[]{"CLOSED", 10L},
                new Object[]{"REJECTED", 1L}
        ));
        given(serviceRequestRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(changeRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(changeRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(inspectionRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());

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
        given(serviceRequestRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(serviceRequestRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(changeRepository.countGroupByStatusCd()).willReturn(List.of(
                new Object[]{"DRAFT", 2L},
                new Object[]{"APPROVAL_REQUESTED", 1L},
                new Object[]{"APPROVED", 3L},
                new Object[]{"IN_PROGRESS", 1L},
                new Object[]{"COMPLETED", 5L},
                new Object[]{"CLOSED", 8L},
                new Object[]{"CANCELLED", 1L}
        ));
        given(changeRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(inspectionRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());

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
        given(serviceRequestRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(serviceRequestRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(changeRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(changeRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(inspectionRepository.countGroupByStatusCd()).willReturn(List.of(
                new Object[]{"SCHEDULED", 4L},
                new Object[]{"IN_PROGRESS", 2L},
                new Object[]{"ON_HOLD", 1L},
                new Object[]{"COMPLETED", 6L},
                new Object[]{"CLOSED", 10L}
        ));

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
        given(incidentRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(incidentRepository.countActivePriorityGrouped()).willReturn(Collections.emptyList());
        given(incidentRepository.countSlaOverdue(any())).willReturn(0L);
        given(incidentRepository.countSlaWarning(any())).willReturn(0L);
        given(incidentRepository.findRecentWithCompany(any(Pageable.class))).willReturn(new PageImpl<>(Collections.emptyList()));
        given(incidentRepository.countUnassigned()).willReturn(7L);
        given(incidentRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(serviceRequestRepository.countGroupByStatusCd()).willReturn(
                Collections.singletonList(new Object[]{"PENDING_COMPLETE", 3L}));
        given(serviceRequestRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(changeRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(changeRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(inspectionRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());

        // when
        DashboardStatsResponse result = dashboardService.getStats();

        // then
        assertThat(result.getUnassignedIncidentCount()).isEqualTo(7L);
        assertThat(result.getPendingSrCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("getStats - SLA 초과 건수를 DB 쿼리로 반환한다")
    void getStats_returnsDelayedIncidentCount() {
        // given
        given(incidentRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(incidentRepository.countActivePriorityGrouped()).willReturn(Collections.emptyList());
        given(incidentRepository.countSlaOverdue(any())).willReturn(3L);
        given(incidentRepository.countSlaWarning(any())).willReturn(1L);
        given(incidentRepository.findRecentWithCompany(any(Pageable.class))).willReturn(new PageImpl<>(Collections.emptyList()));
        given(incidentRepository.countUnassigned()).willReturn(0L);
        given(incidentRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(serviceRequestRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(serviceRequestRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(changeRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(changeRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(inspectionRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());

        // when
        DashboardStatsResponse result = dashboardService.getStats();

        // then
        assertThat(result.getSlaOverdueCount()).isEqualTo(3L);
        assertThat(result.getSlaWarningCount()).isEqualTo(1L);
        assertThat(result.getDelayedIncidentCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("getStats - 우선순위별 건수를 집계 쿼리로 반환한다")
    void getStats_returnsPriorityCountsGrouped() {
        // given
        given(incidentRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(incidentRepository.countActivePriorityGrouped()).willReturn(List.of(
                new Object[]{"CRITICAL", 2L},
                new Object[]{"HIGH", 5L},
                new Object[]{"MEDIUM", 3L}
        ));
        given(incidentRepository.countSlaOverdue(any())).willReturn(0L);
        given(incidentRepository.countSlaWarning(any())).willReturn(0L);
        given(incidentRepository.findRecentWithCompany(any(Pageable.class))).willReturn(new PageImpl<>(Collections.emptyList()));
        given(incidentRepository.countUnassigned()).willReturn(0L);
        given(incidentRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(serviceRequestRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(serviceRequestRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(changeRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(changeRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(inspectionRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());

        // when
        DashboardStatsResponse result = dashboardService.getStats();

        // then
        assertThat(result.getPriorityCounts()).containsEntry("CRITICAL", 2L);
        assertThat(result.getPriorityCounts()).containsEntry("HIGH", 5L);
        assertThat(result.getPriorityCounts()).containsEntry("MEDIUM", 3L);
        assertThat(result.getPriorityCounts()).containsEntry("LOW", 0L);
    }

    @Test
    @DisplayName("getStats - 최근 6개월 월별 추이를 반환한다")
    void getStats_returnsMonthlyTrend() {
        // given
        stubAllIncidentDefaults();
        given(serviceRequestRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(changeRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(inspectionRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
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
        given(incidentRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(incidentRepository.countActivePriorityGrouped()).willReturn(Collections.emptyList());
        given(incidentRepository.countSlaOverdue(any())).willReturn(0L);
        given(incidentRepository.countSlaWarning(any())).willReturn(0L);
        given(incidentRepository.findRecentWithCompany(any(Pageable.class))).willReturn(new PageImpl<>(Collections.emptyList()));
        given(incidentRepository.countUnassigned()).willReturn(0L);
        given(incidentRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
    }

    @Test
    @DisplayName("getStats - 모든 데이터가 비어있어도 0으로 초기화된 통계를 반환한다")
    void getStats_emptyData_returnsZeroCounts() {
        // given
        stubAllIncidentDefaults();
        given(serviceRequestRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(serviceRequestRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(changeRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(changeRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(inspectionRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());

        // when
        DashboardStatsResponse result = dashboardService.getStats();

        // then
        assertThat(result.getTotalIncidentCount()).isZero();
        assertThat(result.getTotalSrCount()).isZero();
        assertThat(result.getTotalChangeCount()).isZero();
        assertThat(result.getTotalInspectionCount()).isZero();
        assertThat(result.getPriorityCounts()).containsEntry("CRITICAL", 0L);
        assertThat(result.getPriorityCounts()).containsEntry("HIGH", 0L);
        assertThat(result.getPriorityCounts()).containsEntry("MEDIUM", 0L);
        assertThat(result.getPriorityCounts()).containsEntry("LOW", 0L);
        assertThat(result.getRecentIncidents()).isEmpty();
        assertThat(result.getMonthlyTrend()).hasSize(6);
    }

    @Test
    @DisplayName("getStats - 최근 인시던트 목록을 회사명과 함께 매핑한다")
    void getStats_returnsRecentIncidentsWithCompany() {
        // given
        Company company = Company.builder().companyNm("ACME").build();
        ReflectionTestUtils.setField(company, "companyId", 1L);
        Incident inc = Incident.builder()
                .title("Server Down")
                .priorityCd("HIGH")
                .occurredAt(LocalDateTime.now())
                .company(company)
                .build();
        ReflectionTestUtils.setField(inc, "incidentId", 100L);

        given(incidentRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(incidentRepository.countActivePriorityGrouped()).willReturn(Collections.emptyList());
        given(incidentRepository.countSlaOverdue(any())).willReturn(0L);
        given(incidentRepository.countSlaWarning(any())).willReturn(0L);
        given(incidentRepository.findRecentWithCompany(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(inc)));
        given(incidentRepository.countUnassigned()).willReturn(0L);
        given(incidentRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(serviceRequestRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(serviceRequestRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(changeRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(changeRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(inspectionRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());

        // when
        DashboardStatsResponse result = dashboardService.getStats();

        // then
        assertThat(result.getRecentIncidents()).hasSize(1);
        IncidentResponse mapped = result.getRecentIncidents().get(0);
        assertThat(mapped.getIncidentId()).isEqualTo(100L);
        assertThat(mapped.getTitle()).isEqualTo("Server Down");
        assertThat(mapped.getCompanyNm()).isEqualTo("ACME");
        assertThat(mapped.getPriorityCd()).isEqualTo("HIGH");
    }

    @Test
    @DisplayName("getStats - 회사가 null인 인시던트도 매핑한다")
    void getStats_recentIncidentsWithNullCompany() {
        // given
        Incident inc = Incident.builder()
                .title("Standalone Incident")
                .priorityCd("LOW")
                .occurredAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(inc, "incidentId", 200L);

        given(incidentRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(incidentRepository.countActivePriorityGrouped()).willReturn(Collections.emptyList());
        given(incidentRepository.countSlaOverdue(any())).willReturn(0L);
        given(incidentRepository.countSlaWarning(any())).willReturn(0L);
        given(incidentRepository.findRecentWithCompany(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(inc)));
        given(incidentRepository.countUnassigned()).willReturn(0L);
        given(incidentRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(serviceRequestRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(serviceRequestRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(changeRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(changeRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(inspectionRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());

        // when
        DashboardStatsResponse result = dashboardService.getStats();

        // then
        assertThat(result.getRecentIncidents()).hasSize(1);
        assertThat(result.getRecentIncidents().get(0).getCompanyNm()).isNull();
    }

    @Test
    @DisplayName("getStats - 월별 추이는 yyyy-MM 형식의 6개월을 시간 순서대로 반환한다")
    void getStats_monthlyTrendOrderedAscending() {
        // given
        stubAllIncidentDefaults();
        given(serviceRequestRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(serviceRequestRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(changeRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());
        given(changeRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
        given(inspectionRepository.countGroupByStatusCd()).willReturn(Collections.emptyList());

        // when
        DashboardStatsResponse result = dashboardService.getStats();

        // then
        assertThat(result.getMonthlyTrend()).hasSize(6);
        for (MonthlyTrendItem item : result.getMonthlyTrend()) {
            assertThat(item.getMonth()).matches("\\d{4}-\\d{2}");
        }
        // 시간 순서 보장 (오름차순)
        String first = result.getMonthlyTrend().get(0).getMonth();
        String last = result.getMonthlyTrend().get(5).getMonth();
        assertThat(first.compareTo(last)).isLessThanOrEqualTo(0);
    }
}
