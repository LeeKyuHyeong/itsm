package com.itsm.api.service.servicerequest;

import com.itsm.api.dto.servicerequest.*;
import com.itsm.core.domain.common.SlaPolicy;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.servicerequest.*;
import com.itsm.core.domain.user.User;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.common.SlaPolicyRepository;
import com.itsm.core.repository.company.CompanyRepository;
import com.itsm.core.repository.servicerequest.*;
import com.itsm.core.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ServiceRequestServiceTest {

    @Mock private ServiceRequestRepository serviceRequestRepository;
    @Mock private ServiceRequestAssigneeRepository assigneeRepository;
    @Mock private ServiceRequestProcessRepository processRepository;
    @Mock private ServiceRequestHistoryRepository historyRepository;
    @Mock private ServiceRequestAssetRepository assetRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private UserRepository userRepository;
    @Mock private SlaPolicyRepository slaPolicyRepository;

    @InjectMocks
    private ServiceRequestService serviceRequestService;

    private Company company;
    private User user;
    private ServiceRequest serviceRequest;

    @BeforeEach
    void setUp() {
        company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        ReflectionTestUtils.setField(company, "companyId", 1L);

        user = User.builder().loginId("user01").password("pw").userNm("테스트유저").build();
        ReflectionTestUtils.setField(user, "userId", 10L);

        serviceRequest = ServiceRequest.builder()
                .title("프린터 설치 요청")
                .content("3층 프린터 설치 부탁드립니다")
                .requestTypeCd("INSTALL")
                .priorityCd("MEDIUM")
                .occurredAt(LocalDateTime.of(2026, 3, 13, 10, 0))
                .company(company)
                .build();
        ReflectionTestUtils.setField(serviceRequest, "requestId", 1L);
    }

    @Test
    @DisplayName("서비스요청 목록을 검색 조건으로 조회한다")
    void search_returnsList() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<ServiceRequest> page = new PageImpl<>(List.of(serviceRequest), pageable, 1);
        given(serviceRequestRepository.search(null, null, null, null, null, pageable)).willReturn(page);

        Page<SrResponse> result = serviceRequestService.search(null, null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("프린터 설치 요청");
    }

    @Test
    @DisplayName("서비스요청 상세를 조회한다")
    void getDetail_returnsResponse() {
        given(serviceRequestRepository.findById(1L)).willReturn(Optional.of(serviceRequest));
        given(assigneeRepository.findByRequestId(1L)).willReturn(List.of());

        SrResponse result = serviceRequestService.getDetail(1L);

        assertThat(result.getRequestId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("프린터 설치 요청");
    }

    @Test
    @DisplayName("존재하지 않는 서비스요청 조회 시 예외가 발생한다")
    void getDetail_notFound_throwsException() {
        given(serviceRequestRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> serviceRequestService.getDetail(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("서비스요청을 성공적으로 생성한다")
    void create_success() {
        SrCreateRequest req = new SrCreateRequest(
                "프린터 설치 요청", "3층 프린터 설치 부탁드립니다", "INSTALL", "MEDIUM",
                LocalDateTime.of(2026, 3, 13, 10, 0), 1L);

        given(companyRepository.findById(1L)).willReturn(Optional.of(company));
        SlaPolicy slaPolicy = SlaPolicy.builder()
                .priorityCd("MEDIUM").deadlineHours(8).warningPct(80).build();
        given(slaPolicyRepository.findByCompanyIdAndPriorityCd(1L, "MEDIUM")).willReturn(Optional.of(slaPolicy));
        given(serviceRequestRepository.save(any(ServiceRequest.class))).willAnswer(inv -> {
            ServiceRequest saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "requestId", 2L);
            return saved;
        });

        SrResponse result = serviceRequestService.create(req, 1L);

        assertThat(result.getTitle()).isEqualTo("프린터 설치 요청");
        assertThat(result.getStatusCd()).isEqualTo("RECEIVED");
        assertThat(result.getSlaDeadlineAt()).isNotNull();
        verify(serviceRequestRepository).save(any(ServiceRequest.class));
    }

    @Test
    @DisplayName("서비스요청을 수정하면 변경 이력이 저장된다")
    void update_savesHistory() {
        SrUpdateRequest req = new SrUpdateRequest(
                "프린터 설치 요청 - 수정", "내용 수정", "REPAIR", "HIGH",
                LocalDateTime.of(2026, 3, 13, 11, 0));

        given(serviceRequestRepository.findById(1L)).willReturn(Optional.of(serviceRequest));

        SrResponse result = serviceRequestService.update(1L, req, 1L);

        assertThat(result.getTitle()).isEqualTo("프린터 설치 요청 - 수정");
        verify(historyRepository).saveAll(any());
    }

    @Test
    @DisplayName("서비스요청 상태를 변경한다")
    void changeStatus_success() {
        given(serviceRequestRepository.findById(1L)).willReturn(Optional.of(serviceRequest));

        serviceRequestService.changeStatus(1L, "ASSIGNED", 1L);

        assertThat(serviceRequest.getStatusCd()).isEqualTo("ASSIGNED");
        verify(historyRepository).save(any(ServiceRequestHistory.class));
    }

    @Test
    @DisplayName("유효하지 않은 상태 전이 시 예외가 발생한다")
    void changeStatus_invalidTransition_throwsException() {
        given(serviceRequestRepository.findById(1L)).willReturn(Optional.of(serviceRequest));

        assertThatThrownBy(() -> serviceRequestService.changeStatus(1L, "CLOSED", 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_STATE_TRANSITION);
    }

    @Test
    @DisplayName("REJECTED 상태 변경 시 SLA가 연장된다")
    void changeStatus_rejected_extendsSla() {
        serviceRequest.setSlaDeadline(LocalDateTime.of(2026, 3, 13, 18, 0));
        given(serviceRequestRepository.findById(1L)).willReturn(Optional.of(serviceRequest));
        SlaPolicy slaPolicy = SlaPolicy.builder()
                .priorityCd("MEDIUM").deadlineHours(8).warningPct(80).build();
        given(slaPolicyRepository.findByCompanyIdAndPriorityCd(1L, "MEDIUM")).willReturn(Optional.of(slaPolicy));

        serviceRequestService.changeStatus(1L, "REJECTED", 1L);

        assertThat(serviceRequest.getStatusCd()).isEqualTo("REJECTED");
        assertThat(serviceRequest.getRejectCnt()).isEqualTo(1);
        assertThat(serviceRequest.getSlaDeadlineAt()).isEqualTo(LocalDateTime.of(2026, 3, 14, 2, 0));
    }

    @Test
    @DisplayName("담당자를 지정한다")
    void assignUser_success() {
        given(serviceRequestRepository.findById(1L)).willReturn(Optional.of(serviceRequest));
        given(userRepository.findById(10L)).willReturn(Optional.of(user));
        given(assigneeRepository.findById(any())).willReturn(Optional.empty());
        given(assigneeRepository.save(any(ServiceRequestAssignee.class))).willAnswer(inv -> inv.getArgument(0));

        SrAssigneeResponse result = serviceRequestService.assignUser(1L, 10L, 1L);

        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getUserNm()).isEqualTo("테스트유저");
        assertThat(result.getProcessStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("이미 지정된 담당자 재지정 시 예외가 발생한다")
    void assignUser_duplicate_throwsException() {
        ServiceRequestAssignee existing = ServiceRequestAssignee.builder()
                .requestId(1L).userId(10L).grantedBy(1L).build();
        given(serviceRequestRepository.findById(1L)).willReturn(Optional.of(serviceRequest));
        given(userRepository.findById(10L)).willReturn(Optional.of(user));
        given(assigneeRepository.findById(any())).willReturn(Optional.of(existing));

        assertThatThrownBy(() -> serviceRequestService.assignUser(1L, 10L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_VALUE);
    }

    @Test
    @DisplayName("담당자를 해제한다")
    void removeAssignee_success() {
        ServiceRequestAssignee assignee = ServiceRequestAssignee.builder()
                .requestId(1L).userId(10L).grantedBy(1L).build();
        given(assigneeRepository.findById(any())).willReturn(Optional.of(assignee));

        serviceRequestService.removeAssignee(1L, 10L);

        verify(assigneeRepository).delete(assignee);
    }

    @Test
    @DisplayName("담당자 목록을 조회한다")
    void getAssignees_returnsList() {
        ServiceRequestAssignee assignee = ServiceRequestAssignee.builder()
                .requestId(1L).userId(10L).grantedBy(1L).build();
        ReflectionTestUtils.setField(assignee, "user", user);
        given(assigneeRepository.findByRequestId(1L)).willReturn(List.of(assignee));

        List<SrAssigneeResponse> result = serviceRequestService.getAssignees(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserNm()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("처리내용을 등록한다")
    void addProcess_success() {
        given(serviceRequestRepository.findById(1L)).willReturn(Optional.of(serviceRequest));
        given(processRepository.save(any(ServiceRequestProcess.class))).willAnswer(inv -> {
            ServiceRequestProcess p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "processId", 1L);
            return p;
        });

        SrProcessResponse result = serviceRequestService.addProcess(1L, 10L, "프린터 설치 진행 중");

        assertThat(result.getProcessContent()).isEqualTo("프린터 설치 진행 중");
        assertThat(result.getIsCompleted()).isEqualTo("N");
    }

    @Test
    @DisplayName("처리내용을 완료 처리한다")
    void completeProcess_success() {
        ServiceRequestProcess process = ServiceRequestProcess.builder()
                .requestId(1L).userId(10L).processContent("설치 완료").build();
        ReflectionTestUtils.setField(process, "processId", 1L);
        given(processRepository.findById(1L)).willReturn(Optional.of(process));

        serviceRequestService.completeProcess(1L, 1L);

        assertThat(process.getIsCompleted()).isEqualTo("Y");
    }

    @Test
    @DisplayName("처리내용 목록을 조회한다")
    void getProcesses_returnsList() {
        ServiceRequestProcess process = ServiceRequestProcess.builder()
                .requestId(1L).userId(10L).processContent("진행 중").build();
        ReflectionTestUtils.setField(process, "processId", 1L);
        given(processRepository.findByRequestIdOrderByCreatedAtAsc(1L)).willReturn(List.of(process));

        List<SrProcessResponse> result = serviceRequestService.getProcesses(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProcessContent()).isEqualTo("진행 중");
    }

    @Test
    @DisplayName("만족도를 제출한다")
    void submitSatisfaction_success() {
        serviceRequest.changeStatus("ASSIGNED");
        serviceRequest.changeStatus("IN_PROGRESS");
        serviceRequest.changeStatus("PENDING_COMPLETE");
        serviceRequest.changeStatus("CLOSED");

        given(serviceRequestRepository.findById(1L)).willReturn(Optional.of(serviceRequest));

        serviceRequestService.submitSatisfaction(1L, 5, "만족합니다");

        assertThat(serviceRequest.getSatisfactionScore()).isEqualTo(5);
        assertThat(serviceRequest.getSatisfactionComment()).isEqualTo("만족합니다");
    }

    @Test
    @DisplayName("CLOSED 상태가 아닌 요청에 만족도 제출 시 예외가 발생한다")
    void submitSatisfaction_notClosed_throwsException() {
        given(serviceRequestRepository.findById(1L)).willReturn(Optional.of(serviceRequest));

        assertThatThrownBy(() -> serviceRequestService.submitSatisfaction(1L, 5, "만족합니다"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("변경 이력을 조회한다")
    void getHistory_returnsList() {
        ServiceRequestHistory history = ServiceRequestHistory.builder()
                .requestId(1L).changedField("statusCd")
                .beforeValue("RECEIVED").afterValue("ASSIGNED")
                .createdBy(1L).build();
        ReflectionTestUtils.setField(history, "historyId", 1L);
        given(historyRepository.findByRequestIdOrderByCreatedAtDesc(1L)).willReturn(List.of(history));

        List<SrHistoryResponse> result = serviceRequestService.getHistory(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChangedField()).isEqualTo("statusCd");
    }

    @Test
    @DisplayName("모든 담당자가 COMPLETED이면 자동으로 PENDING_COMPLETE로 전이된다")
    void checkAutoTransition_allCompleted_transitionsToPendingComplete() {
        serviceRequest.changeStatus("ASSIGNED");
        serviceRequest.changeStatus("IN_PROGRESS");

        ServiceRequestAssignee assignee1 = ServiceRequestAssignee.builder()
                .requestId(1L).userId(10L).grantedBy(1L).build();
        assignee1.changeProcessStatus("COMPLETED");
        ServiceRequestAssignee assignee2 = ServiceRequestAssignee.builder()
                .requestId(1L).userId(20L).grantedBy(1L).build();
        assignee2.changeProcessStatus("COMPLETED");

        given(serviceRequestRepository.findById(1L)).willReturn(Optional.of(serviceRequest));
        given(assigneeRepository.findByRequestId(1L)).willReturn(List.of(assignee1, assignee2));

        serviceRequestService.checkAutoTransition(1L, 1L);

        assertThat(serviceRequest.getStatusCd()).isEqualTo("PENDING_COMPLETE");
    }

    @Test
    @DisplayName("SLA 경과율을 계산한다")
    void calculateSlaPercentage_returnsCorrectValue() {
        LocalDateTime occurredAt = LocalDateTime.now().minusHours(2);
        LocalDateTime deadline = LocalDateTime.now().plusHours(2);
        ReflectionTestUtils.setField(serviceRequest, "occurredAt", occurredAt);
        serviceRequest.setSlaDeadline(deadline);

        given(serviceRequestRepository.findById(1L)).willReturn(Optional.of(serviceRequest));
        given(assigneeRepository.findByRequestId(1L)).willReturn(List.of());

        SrResponse result = serviceRequestService.getDetail(1L);

        assertThat(result.getSlaPercentage()).isBetween(40.0, 60.0);
    }
}
