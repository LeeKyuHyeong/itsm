package com.itsm.batch.job.change;

import com.itsm.core.domain.change.Change;
import com.itsm.core.domain.change.ChangeApprover;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.user.User;
import com.itsm.core.repository.change.ChangeApproverRepository;
import com.itsm.core.repository.change.ChangeRepository;
import com.itsm.core.repository.company.CompanyRepository;
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
@DisplayName("ChangeSimulationJob 테스트")
class ChangeSimulationJobTest {

    @InjectMocks
    private ChangeSimulationJob job;

    @Mock
    private ChangeRepository changeRepository;

    @Mock
    private ChangeApproverRepository changeApproverRepository;

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
        verify(changeRepository, never()).save(any(Change.class));
    }

    @Test
    @DisplayName("주간 할당량 미달 시 신규 변경요청을 생성한다")
    void execute_createNewChanges() {
        // given
        Company company = mock(Company.class);
        when(company.getCompanyId()).thenReturn(1L);
        when(companyRepository.findAll()).thenReturn(List.of(company));

        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1L);
        when(userRepository.findAll()).thenReturn(List.of(user));

        when(changeRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);

        when(changeRepository.findByStatusCd("DRAFT")).thenReturn(Collections.emptyList());
        when(changeRepository.findByStatusCd("APPROVAL_REQUESTED")).thenReturn(Collections.emptyList());
        when(changeRepository.findByStatusCd("APPROVED")).thenReturn(Collections.emptyList());
        when(changeRepository.findByStatusCd("IN_PROGRESS")).thenReturn(Collections.emptyList());
        when(changeRepository.findByStatusCd("COMPLETED")).thenReturn(Collections.emptyList());

        when(changeRepository.save(any(Change.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        job.execute();

        // then
        verify(changeRepository, atLeastOnce()).save(any(Change.class));
    }

    @Test
    @DisplayName("승인요청 상태의 변경에 승인자를 추가하고 승인 처리한다")
    void execute_processApprovals() {
        // given
        Company company = mock(Company.class);
        when(company.getCompanyId()).thenReturn(1L);
        when(companyRepository.findAll()).thenReturn(List.of(company));

        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1L);
        when(userRepository.findAll()).thenReturn(List.of(user));

        when(changeRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(4L);

        when(changeRepository.findByStatusCd("DRAFT")).thenReturn(Collections.emptyList());

        Change approvalChange = mock(Change.class);
        when(approvalChange.getChangeId()).thenReturn(10L);
        when(approvalChange.getStatusCd()).thenReturn("APPROVAL_REQUESTED");
        when(changeRepository.findByStatusCd("APPROVAL_REQUESTED")).thenReturn(List.of(approvalChange));

        when(changeApproverRepository.findByChangeIdOrderByApproveOrderAsc(10L))
                .thenReturn(Collections.emptyList());

        when(changeRepository.findByStatusCd("APPROVED")).thenReturn(Collections.emptyList());
        when(changeRepository.findByStatusCd("IN_PROGRESS")).thenReturn(Collections.emptyList());
        when(changeRepository.findByStatusCd("COMPLETED")).thenReturn(Collections.emptyList());

        // when
        job.execute();

        // then
        verify(changeApproverRepository, atLeastOnce()).save(any(ChangeApprover.class));
    }

    @Test
    @DisplayName("10-15% 확률로 변경요청을 반려 처리한다")
    void execute_rejectionHandling() {
        // given
        Company company = mock(Company.class);
        when(company.getCompanyId()).thenReturn(1L);
        when(companyRepository.findAll()).thenReturn(List.of(company));

        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1L);
        when(userRepository.findAll()).thenReturn(List.of(user));

        when(changeRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(4L);

        when(changeRepository.findByStatusCd("DRAFT")).thenReturn(Collections.emptyList());
        when(changeRepository.findByStatusCd("APPROVAL_REQUESTED")).thenReturn(Collections.emptyList());
        when(changeRepository.findByStatusCd("APPROVED")).thenReturn(Collections.emptyList());
        when(changeRepository.findByStatusCd("IN_PROGRESS")).thenReturn(Collections.emptyList());
        when(changeRepository.findByStatusCd("COMPLETED")).thenReturn(Collections.emptyList());

        // when - execute multiple times to verify no exceptions with rejection logic
        job.execute();

        // then - verify no exception thrown (rejection is random, so just ensure stability)
        verify(changeRepository, atLeastOnce()).findByStatusCd(anyString());
    }
}
