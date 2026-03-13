package com.itsm.batch.job.inspection;

import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.inspection.Inspection;
import com.itsm.core.domain.inspection.InspectionItem;
import com.itsm.core.domain.inspection.InspectionResult;
import com.itsm.core.domain.user.User;
import com.itsm.core.repository.company.CompanyRepository;
import com.itsm.core.repository.incident.IncidentRepository;
import com.itsm.core.repository.inspection.InspectionItemRepository;
import com.itsm.core.repository.inspection.InspectionRepository;
import com.itsm.core.repository.inspection.InspectionResultRepository;
import com.itsm.core.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("InspectionSimulationJob 테스트")
class InspectionSimulationJobTest {

    @InjectMocks
    private InspectionSimulationJob job;

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private InspectionItemRepository inspectionItemRepository;

    @Mock
    private InspectionResultRepository inspectionResultRepository;

    @Mock
    private IncidentRepository incidentRepository;

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
        verify(inspectionRepository, never()).save(any(Inspection.class));
    }

    @Test
    @DisplayName("예정된 점검을 자동 생성한다")
    void execute_createInspections() {
        // given
        Company company = mock(Company.class);
        when(company.getCompanyId()).thenReturn(1L);
        when(companyRepository.findAll()).thenReturn(List.of(company));

        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1L);
        when(userRepository.findAll()).thenReturn(List.of(user));

        when(inspectionRepository.findByStatusCdAndScheduledAtBetween(eq("SCHEDULED"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        when(inspectionRepository.findByStatusCdAndScheduledAtBefore(eq("SCHEDULED"), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(inspectionRepository.findByStatusCd("IN_PROGRESS")).thenReturn(Collections.emptyList());
        when(inspectionRepository.findByStatusCd("COMPLETED")).thenReturn(Collections.emptyList());

        when(inspectionRepository.save(any(Inspection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        job.execute();

        // then
        verify(inspectionRepository, atLeastOnce()).save(any(Inspection.class));
    }

    @Test
    @DisplayName("진행중 점검에 항목과 결과를 입력하고 완료 처리한다")
    void execute_completeWithResults() {
        // given
        Company company = mock(Company.class);
        when(company.getCompanyId()).thenReturn(1L);
        when(companyRepository.findAll()).thenReturn(List.of(company));

        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1L);
        when(userRepository.findAll()).thenReturn(List.of(user));

        when(inspectionRepository.findByStatusCdAndScheduledAtBetween(eq("SCHEDULED"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        when(inspectionRepository.findByStatusCdAndScheduledAtBefore(eq("SCHEDULED"), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        Inspection inProgressInspection = mock(Inspection.class);
        when(inProgressInspection.getInspectionId()).thenReturn(10L);
        when(inProgressInspection.getInspectionTypeCd()).thenReturn("SERVER");
        when(inProgressInspection.getStatusCd()).thenReturn("IN_PROGRESS");
        when(inspectionRepository.findByStatusCd("IN_PROGRESS")).thenReturn(List.of(inProgressInspection));

        when(inspectionItemRepository.countByInspectionId(10L)).thenReturn(0L);

        InspectionItem item = mock(InspectionItem.class);
        when(item.getItemId()).thenReturn(100L);
        when(inspectionItemRepository.findByInspectionIdOrderBySortOrderAsc(10L))
                .thenReturn(List.of(item));
        when(inspectionItemRepository.save(any(InspectionItem.class))).thenAnswer(invocation -> {
            InspectionItem saved = invocation.getArgument(0);
            // mock a saved item with id
            InspectionItem mockItem = mock(InspectionItem.class);
            when(mockItem.getItemId()).thenReturn(100L);
            return mockItem;
        });

        when(inspectionResultRepository.countByInspectionId(10L)).thenReturn(0L);

        when(inspectionRepository.findByStatusCd("COMPLETED")).thenReturn(Collections.emptyList());

        // when
        job.execute();

        // then
        verify(inspectionItemRepository, atLeastOnce()).save(any(InspectionItem.class));
        verify(inspectionResultRepository, atLeastOnce()).save(any(InspectionResult.class));
        verify(inProgressInspection).changeStatus("COMPLETED");
    }

    @Test
    @DisplayName("비정상 점검 결과 발견 시 장애를 자동 생성한다")
    void execute_abnormalCreatesIncident() {
        // given
        Company company = mock(Company.class);
        when(company.getCompanyId()).thenReturn(1L);
        when(companyRepository.findAll()).thenReturn(List.of(company));

        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1L);
        when(userRepository.findAll()).thenReturn(List.of(user));

        when(inspectionRepository.findByStatusCdAndScheduledAtBetween(eq("SCHEDULED"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        when(inspectionRepository.findByStatusCdAndScheduledAtBefore(eq("SCHEDULED"), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(inspectionRepository.findByStatusCd("IN_PROGRESS")).thenReturn(Collections.emptyList());

        Inspection completedInspection = mock(Inspection.class);
        when(completedInspection.getInspectionId()).thenReturn(20L);
        when(completedInspection.getStatusCd()).thenReturn("COMPLETED");
        when(completedInspection.getCompany()).thenReturn(company);
        when(completedInspection.getTitle()).thenReturn("서버 점검");
        when(inspectionRepository.findByStatusCd("COMPLETED")).thenReturn(List.of(completedInspection));

        InspectionResult abnormalResult = mock(InspectionResult.class);
        when(abnormalResult.getIsNormal()).thenReturn("N");
        when(abnormalResult.getResultValue()).thenReturn("비정상 (CPU 95%)");
        when(abnormalResult.getItemId()).thenReturn(100L);

        InspectionItem item = mock(InspectionItem.class);
        when(item.getItemNm()).thenReturn("CPU 사용률 확인");

        when(inspectionResultRepository.findByInspectionId(20L)).thenReturn(List.of(abnormalResult));
        when(inspectionItemRepository.findByInspectionIdOrderBySortOrderAsc(20L)).thenReturn(List.of(item));

        // when
        job.execute();

        // then
        verify(incidentRepository, atLeastOnce()).save(any());
        verify(completedInspection).changeStatus("CLOSED");
    }
}
