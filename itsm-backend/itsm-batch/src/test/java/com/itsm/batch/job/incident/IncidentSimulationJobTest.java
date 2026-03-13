package com.itsm.batch.job.incident;

import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.incident.Incident;
import com.itsm.core.domain.user.User;
import com.itsm.core.repository.asset.AssetHwRepository;
import com.itsm.core.repository.company.CompanyRepository;
import com.itsm.core.repository.incident.IncidentAssigneeRepository;
import com.itsm.core.repository.incident.IncidentCommentRepository;
import com.itsm.core.repository.incident.IncidentRepository;
import com.itsm.core.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("IncidentSimulationJob 테스트")
class IncidentSimulationJobTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private IncidentCommentRepository incidentCommentRepository;

    @Mock
    private IncidentAssigneeRepository incidentAssigneeRepository;

    @Mock
    private AssetHwRepository assetHwRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private IncidentSimulationJob incidentSimulationJob;

    @Test
    @DisplayName("회사 데이터가 없으면 장애 시뮬레이션을 건너뛴다")
    void execute_noCompany_skip() {
        // given
        when(companyRepository.findAll()).thenReturn(Collections.emptyList());

        // when
        incidentSimulationJob.execute();

        // then
        verify(incidentRepository, never()).save(any());
    }

    @Test
    @DisplayName("사용자 데이터가 없으면 장애 시뮬레이션을 건너뛴다")
    void execute_noUser_skip() {
        // given
        Company company = mock(Company.class);
        when(companyRepository.findAll()).thenReturn(List.of(company));
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // when
        incidentSimulationJob.execute();

        // then
        verify(incidentRepository, never()).save(any());
    }

    @Test
    @DisplayName("일일 할당량 미달 시 신규 장애를 생성한다")
    void execute_createNewIncidents() {
        // given
        Company company = mock(Company.class);
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1L);

        when(companyRepository.findAll()).thenReturn(List.of(company));
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(incidentRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(incidentRepository.findByStatusCd("RECEIVED")).thenReturn(Collections.emptyList());
        when(incidentRepository.findByStatusCd("IN_PROGRESS")).thenReturn(Collections.emptyList());
        when(incidentRepository.findByStatusCd("COMPLETED")).thenReturn(Collections.emptyList());

        Incident savedIncident = mock(Incident.class);
        when(savedIncident.getIncidentId()).thenReturn(1L);
        when(incidentRepository.save(any(Incident.class))).thenReturn(savedIncident);

        // when
        incidentSimulationJob.execute();

        // then: 2~5건 장애 생성
        verify(incidentRepository, atLeast(2)).save(any(Incident.class));
        verify(incidentRepository, atMost(5)).save(any(Incident.class));
    }

    @Test
    @DisplayName("RECEIVED 상태 장애를 자동 배정 처리한다")
    void execute_assignUnassigned() {
        // given
        Company company = mock(Company.class);
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1L);

        when(companyRepository.findAll()).thenReturn(List.of(company));
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(incidentRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5L); // 할당량 충족하여 신규 생성 건너뜀

        Incident receivedIncident = mock(Incident.class);
        when(receivedIncident.getIncidentId()).thenReturn(10L);
        when(receivedIncident.getCreatedAt()).thenReturn(LocalDateTime.now().minusHours(2)); // 충분히 오래됨

        when(incidentRepository.findByStatusCd("RECEIVED")).thenReturn(List.of(receivedIncident));
        when(incidentRepository.findByStatusCd("IN_PROGRESS")).thenReturn(Collections.emptyList());
        when(incidentRepository.findByStatusCd("COMPLETED")).thenReturn(Collections.emptyList());

        // when
        incidentSimulationJob.execute();

        // then
        verify(receivedIncident).assignMainManager(any(User.class));
        verify(receivedIncident).changeStatus("IN_PROGRESS");
        verify(incidentAssigneeRepository, atLeastOnce()).save(any());
        verify(incidentCommentRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("IN_PROGRESS 상태 장애를 자동 완료 처리한다")
    void execute_completeInProgress() {
        // given
        Company company = mock(Company.class);
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1L);

        when(companyRepository.findAll()).thenReturn(List.of(company));
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(incidentRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5L);

        Incident inProgressIncident = mock(Incident.class);
        when(inProgressIncident.getIncidentId()).thenReturn(20L);
        when(inProgressIncident.getCreatedAt()).thenReturn(LocalDateTime.now().minusHours(12)); // 충분히 오래됨
        when(inProgressIncident.getPriorityCd()).thenReturn("MEDIUM");

        when(incidentRepository.findByStatusCd("RECEIVED")).thenReturn(Collections.emptyList());
        when(incidentRepository.findByStatusCd("IN_PROGRESS")).thenReturn(List.of(inProgressIncident));
        when(incidentRepository.findByStatusCd("COMPLETED")).thenReturn(Collections.emptyList());

        // when
        incidentSimulationJob.execute();

        // then
        verify(inProgressIncident).changeStatus("COMPLETED");
        verify(inProgressIncident).writeProcessContent(anyString());
        verify(incidentCommentRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("COMPLETED 상태 장애를 자동 종료 처리한다")
    void execute_closeCompleted() {
        // given
        Company company = mock(Company.class);
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1L);

        when(companyRepository.findAll()).thenReturn(List.of(company));
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(incidentRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5L);

        Incident completedIncident = mock(Incident.class);
        when(completedIncident.getIncidentId()).thenReturn(30L);
        when(completedIncident.getCompletedAt()).thenReturn(LocalDateTime.now().minusDays(2)); // 충분히 오래됨

        when(incidentRepository.findByStatusCd("RECEIVED")).thenReturn(Collections.emptyList());
        when(incidentRepository.findByStatusCd("IN_PROGRESS")).thenReturn(Collections.emptyList());
        when(incidentRepository.findByStatusCd("COMPLETED")).thenReturn(List.of(completedIncident));

        // when
        incidentSimulationJob.execute();

        // then
        verify(completedIncident).changeStatus("CLOSED");
        verify(incidentCommentRepository, atLeastOnce()).save(any());
    }
}
