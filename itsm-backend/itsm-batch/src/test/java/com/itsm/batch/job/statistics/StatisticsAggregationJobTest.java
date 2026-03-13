package com.itsm.batch.job.statistics;

import com.itsm.core.domain.incident.Incident;
import com.itsm.core.domain.servicerequest.ServiceRequest;
import com.itsm.core.domain.statistics.DailyStatistics;
import com.itsm.core.repository.asset.AssetHwRepository;
import com.itsm.core.repository.asset.AssetSwRepository;
import com.itsm.core.repository.change.ChangeRepository;
import com.itsm.core.repository.incident.IncidentRepository;
import com.itsm.core.repository.log.LoginHistoryRepository;
import com.itsm.core.repository.log.SimMenuAccessLogRepository;
import com.itsm.core.repository.servicerequest.ServiceRequestRepository;
import com.itsm.core.repository.statistics.DailyStatisticsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatisticsAggregationJob 테스트")
class StatisticsAggregationJobTest {

    @InjectMocks
    private StatisticsAggregationJob job;

    @Mock
    private DailyStatisticsRepository dailyStatisticsRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @Mock
    private ChangeRepository changeRepository;

    @Mock
    private AssetHwRepository assetHwRepository;

    @Mock
    private AssetSwRepository assetSwRepository;

    @Mock
    private LoginHistoryRepository loginHistoryRepository;

    @Mock
    private SimMenuAccessLogRepository menuAccessLogRepository;

    private void stubAllReposEmpty() {
        lenient().when(incidentRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
        lenient().when(incidentRepository.findByStatusCd(anyString())).thenReturn(Collections.emptyList());
        lenient().when(incidentRepository.findByStatusCdIn(any())).thenReturn(Collections.emptyList());
        lenient().when(serviceRequestRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
        lenient().when(serviceRequestRepository.findByStatusCd(anyString())).thenReturn(Collections.emptyList());
        lenient().when(serviceRequestRepository.countByStatusCd(anyString())).thenReturn(0L);
        lenient().when(changeRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
        lenient().when(changeRepository.countByStatusCd(anyString())).thenReturn(0L);
        lenient().when(assetHwRepository.count()).thenReturn(0L);
        lenient().when(assetSwRepository.count()).thenReturn(0L);
        lenient().when(assetHwRepository.countByStatus()).thenReturn(Collections.emptyList());
        lenient().when(assetSwRepository.countByStatus()).thenReturn(Collections.emptyList());
        lenient().when(loginHistoryRepository.countByLoginAtBetween(any(), any())).thenReturn(0L);
        lenient().when(loginHistoryRepository.findByLoginAtBetween(any(), any())).thenReturn(Collections.emptyList());
        lenient().when(menuAccessLogRepository.countByAccessedAtBetween(any(), any())).thenReturn(0L);
        lenient().when(dailyStatisticsRepository.findByStatDateAndStatTypeAndStatKey(any(), any(), any()))
                .thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("장애 통계를 집계한다")
    void aggregateIncidentStats() {
        // given
        stubAllReposEmpty();

        List<Incident> completedIncidents = createMockIncidents(5, true);
        List<Incident> openIncidents = createMockIncidents(3, false);

        when(incidentRepository.countByCreatedAtBetween(any(), any())).thenReturn(15L);
        when(incidentRepository.findByStatusCd("COMPLETED")).thenReturn(completedIncidents);
        when(incidentRepository.findByStatusCdIn(List.of("RECEIVED", "IN_PROGRESS")))
                .thenReturn(openIncidents);

        // when
        job.execute();

        // then
        ArgumentCaptor<DailyStatistics> captor = ArgumentCaptor.forClass(DailyStatistics.class);
        verify(dailyStatisticsRepository, atLeastOnce()).save(captor.capture());

        List<DailyStatistics> incidentStats = captor.getAllValues().stream()
                .filter(s -> "INCIDENT".equals(s.getStatType()))
                .toList();
        assertThat(incidentStats).isNotEmpty();

        Optional<DailyStatistics> totalCount = incidentStats.stream()
                .filter(s -> "total_count".equals(s.getStatKey()))
                .findFirst();
        assertThat(totalCount).isPresent();
        assertThat(totalCount.get().getStatValue()).isEqualByComparingTo(new BigDecimal("15"));
    }

    @Test
    @DisplayName("서비스요청 통계를 집계한다")
    void aggregateServiceRequestStats() {
        // given
        stubAllReposEmpty();

        List<ServiceRequest> closedSrs = createMockServiceRequests(3);

        when(serviceRequestRepository.countByCreatedAtBetween(any(), any())).thenReturn(8L);
        when(serviceRequestRepository.findByStatusCd("CLOSED")).thenReturn(closedSrs);
        when(serviceRequestRepository.countByStatusCd("RECEIVED")).thenReturn(2L);
        when(serviceRequestRepository.countByStatusCd("ASSIGNED")).thenReturn(1L);
        when(serviceRequestRepository.countByStatusCd("IN_PROGRESS")).thenReturn(3L);

        // when
        job.execute();

        // then
        ArgumentCaptor<DailyStatistics> captor = ArgumentCaptor.forClass(DailyStatistics.class);
        verify(dailyStatisticsRepository, atLeastOnce()).save(captor.capture());

        List<DailyStatistics> srStats = captor.getAllValues().stream()
                .filter(s -> "SERVICE_REQUEST".equals(s.getStatType()))
                .toList();
        assertThat(srStats).isNotEmpty();

        Optional<DailyStatistics> totalCount = srStats.stream()
                .filter(s -> "total_count".equals(s.getStatKey()))
                .findFirst();
        assertThat(totalCount).isPresent();
        assertThat(totalCount.get().getStatValue()).isEqualByComparingTo(new BigDecimal("8"));

        Optional<DailyStatistics> openCount = srStats.stream()
                .filter(s -> "open_count".equals(s.getStatKey()))
                .findFirst();
        assertThat(openCount).isPresent();
        assertThat(openCount.get().getStatValue()).isEqualByComparingTo(new BigDecimal("6"));
    }

    @Test
    @DisplayName("자산 통계를 집계한다")
    void aggregateAssetStats() {
        // given
        stubAllReposEmpty();
        when(assetHwRepository.count()).thenReturn(50L);
        when(assetSwRepository.count()).thenReturn(30L);
        when(assetHwRepository.countByStatus()).thenReturn(List.of(
                new Object[]{"ACTIVE", 40L},
                new Object[]{"DISPOSED", 10L}
        ));
        when(assetSwRepository.countByStatus()).thenReturn(List.of(
                new Object[]{"ACTIVE", 25L},
                new Object[]{"DISPOSED", 5L}
        ));

        // when
        job.execute();

        // then
        ArgumentCaptor<DailyStatistics> captor = ArgumentCaptor.forClass(DailyStatistics.class);
        verify(dailyStatisticsRepository, atLeastOnce()).save(captor.capture());

        List<DailyStatistics> assetStats = captor.getAllValues().stream()
                .filter(s -> "ASSET".equals(s.getStatType()))
                .toList();
        assertThat(assetStats).isNotEmpty();

        Optional<DailyStatistics> hwTotal = assetStats.stream()
                .filter(s -> "hw_total".equals(s.getStatKey()))
                .findFirst();
        assertThat(hwTotal).isPresent();
        assertThat(hwTotal.get().getStatValue()).isEqualByComparingTo(new BigDecimal("50"));

        Optional<DailyStatistics> swTotal = assetStats.stream()
                .filter(s -> "sw_total".equals(s.getStatKey()))
                .findFirst();
        assertThat(swTotal).isPresent();
        assertThat(swTotal.get().getStatValue()).isEqualByComparingTo(new BigDecimal("30"));
    }

    @Test
    @DisplayName("기존 통계가 있으면 업데이트한다")
    void execute_updateExistingStats() {
        // given
        stubAllReposEmpty();
        DailyStatistics existingStat = mock(DailyStatistics.class);
        when(dailyStatisticsRepository.findByStatDateAndStatTypeAndStatKey(any(), eq("INCIDENT"), eq("total_count")))
                .thenReturn(Optional.of(existingStat));

        when(incidentRepository.countByCreatedAtBetween(any(), any())).thenReturn(10L);
        when(incidentRepository.findByStatusCd("COMPLETED")).thenReturn(Collections.emptyList());
        when(incidentRepository.findByStatusCdIn(any())).thenReturn(Collections.emptyList());

        // when
        job.execute();

        // then
        verify(existingStat).updateValue(eq(new BigDecimal("10")), any());
        verify(dailyStatisticsRepository).save(existingStat);
    }

    @Test
    @DisplayName("사용자 활동 통계를 집계한다")
    void aggregateUserStats() {
        // given
        stubAllReposEmpty();
        when(loginHistoryRepository.countByLoginAtBetween(any(), any())).thenReturn(25L);
        when(loginHistoryRepository.findByLoginAtBetween(any(), any())).thenReturn(Collections.emptyList());
        when(menuAccessLogRepository.countByAccessedAtBetween(any(), any())).thenReturn(150L);

        // when
        job.execute();

        // then
        ArgumentCaptor<DailyStatistics> captor = ArgumentCaptor.forClass(DailyStatistics.class);
        verify(dailyStatisticsRepository, atLeastOnce()).save(captor.capture());

        List<DailyStatistics> userStats = captor.getAllValues().stream()
                .filter(s -> "USER".equals(s.getStatType()))
                .toList();
        assertThat(userStats).isNotEmpty();

        Optional<DailyStatistics> loginCount = userStats.stream()
                .filter(s -> "login_count".equals(s.getStatKey()))
                .findFirst();
        assertThat(loginCount).isPresent();
        assertThat(loginCount.get().getStatValue()).isEqualByComparingTo(new BigDecimal("25"));

        Optional<DailyStatistics> pageViewCount = userStats.stream()
                .filter(s -> "page_view_count".equals(s.getStatKey()))
                .findFirst();
        assertThat(pageViewCount).isPresent();
        assertThat(pageViewCount.get().getStatValue()).isEqualByComparingTo(new BigDecimal("150"));
    }

    private List<Incident> createMockIncidents(int count, boolean withCompletedAt) {
        List<Incident> incidents = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Incident incident = mock(Incident.class);
            lenient().doReturn((long) i).when(incident).getIncidentId();
            lenient().doReturn(LocalDateTime.now().minusHours(10)).when(incident).getOccurredAt();
            if (withCompletedAt) {
                lenient().doReturn(LocalDateTime.now().minusHours(2)).when(incident).getCompletedAt();
            }
            lenient().doReturn(LocalDateTime.now().plusHours(5)).when(incident).getSlaDeadlineAt();
            lenient().doReturn(i == 1 ? "CRITICAL" : "MEDIUM").when(incident).getPriorityCd();
            incidents.add(incident);
        }
        return incidents;
    }

    private List<ServiceRequest> createMockServiceRequests(int count) {
        List<ServiceRequest> requests = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            ServiceRequest sr = mock(ServiceRequest.class);
            lenient().doReturn((long) i).when(sr).getRequestId();
            lenient().doReturn(i <= 2 ? 4 : 5).when(sr).getSatisfactionScore();
            requests.add(sr);
        }
        return requests;
    }
}
