package com.itsm.batch.job.servicerequest;

import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.servicerequest.ServiceRequest;
import com.itsm.core.domain.user.User;
import com.itsm.core.repository.company.CompanyRepository;
import com.itsm.core.repository.servicerequest.ServiceRequestAssigneeRepository;
import com.itsm.core.repository.servicerequest.ServiceRequestRepository;
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
@DisplayName("ServiceRequestSimulationJob 테스트")
class ServiceRequestSimulationJobTest {

    @InjectMocks
    private ServiceRequestSimulationJob job;

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @Mock
    private ServiceRequestAssigneeRepository serviceRequestAssigneeRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("회사나 사용자 데이터가 없으면 스킵한다")
    void execute_noData_skip() {
        // given
        when(companyRepository.findAll()).thenReturn(Collections.emptyList());

        // when
        job.execute();

        // then
        verify(serviceRequestRepository, never()).save(any(ServiceRequest.class));
    }

    @Test
    @DisplayName("일일 할당량 미달 시 신규 서비스요청을 생성한다")
    void execute_createNewRequests() {
        // given
        Company company = mock(Company.class);
        when(company.getCompanyId()).thenReturn(1L);
        when(companyRepository.findAll()).thenReturn(List.of(company));

        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1L);
        when(userRepository.findAll()).thenReturn(List.of(user));

        when(serviceRequestRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);

        when(serviceRequestRepository.findByStatusCdAndUpdatedAtBefore(eq("RECEIVED"), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(serviceRequestRepository.findByStatusCdAndUpdatedAtBefore(eq("ASSIGNED"), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(serviceRequestRepository.findByStatusCdAndUpdatedAtBefore(eq("IN_PROGRESS"), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(serviceRequestRepository.findByStatusCdAndUpdatedAtBefore(eq("PENDING_COMPLETE"), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        when(serviceRequestRepository.save(any(ServiceRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        job.execute();

        // then
        verify(serviceRequestRepository, atLeastOnce()).save(any(ServiceRequest.class));
    }

    @Test
    @DisplayName("접수된 SR을 30분 후 자동 배정한다")
    void execute_assignReceived() {
        // given
        Company company = mock(Company.class);
        when(company.getCompanyId()).thenReturn(1L);
        when(companyRepository.findAll()).thenReturn(List.of(company));

        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1L);
        when(userRepository.findAll()).thenReturn(List.of(user));

        when(serviceRequestRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(8L);

        ServiceRequest receivedSr = mock(ServiceRequest.class);
        when(receivedSr.getRequestId()).thenReturn(10L);
        when(receivedSr.getStatusCd()).thenReturn("RECEIVED");
        when(serviceRequestRepository.findByStatusCdAndUpdatedAtBefore(eq("RECEIVED"), any(LocalDateTime.class)))
                .thenReturn(List.of(receivedSr));

        when(serviceRequestRepository.findByStatusCdAndUpdatedAtBefore(eq("ASSIGNED"), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(serviceRequestRepository.findByStatusCdAndUpdatedAtBefore(eq("IN_PROGRESS"), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(serviceRequestRepository.findByStatusCdAndUpdatedAtBefore(eq("PENDING_COMPLETE"), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // when
        job.execute();

        // then
        verify(receivedSr).changeStatus("ASSIGNED");
        verify(serviceRequestAssigneeRepository).save(any());
    }

    @Test
    @DisplayName("완료대기 SR에 만족도 입력 후 마감한다")
    void execute_closeWithSatisfaction() {
        // given
        Company company = mock(Company.class);
        when(company.getCompanyId()).thenReturn(1L);
        when(companyRepository.findAll()).thenReturn(List.of(company));

        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1L);
        when(userRepository.findAll()).thenReturn(List.of(user));

        when(serviceRequestRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(8L);

        when(serviceRequestRepository.findByStatusCdAndUpdatedAtBefore(eq("RECEIVED"), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(serviceRequestRepository.findByStatusCdAndUpdatedAtBefore(eq("ASSIGNED"), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(serviceRequestRepository.findByStatusCdAndUpdatedAtBefore(eq("IN_PROGRESS"), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        ServiceRequest pendingSr = mock(ServiceRequest.class);
        when(pendingSr.getStatusCd()).thenReturn("PENDING_COMPLETE");
        when(serviceRequestRepository.findByStatusCdAndUpdatedAtBefore(eq("PENDING_COMPLETE"), any(LocalDateTime.class)))
                .thenReturn(List.of(pendingSr));

        // when
        job.execute();

        // then
        verify(pendingSr).submitSatisfaction(anyInt(), anyString());
        verify(pendingSr).changeStatus("CLOSED");
    }
}
