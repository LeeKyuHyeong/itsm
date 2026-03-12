package com.itsm.api.service.incident;

import com.itsm.api.dto.incident.*;
import com.itsm.core.domain.common.SlaPolicy;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.incident.*;
import com.itsm.core.domain.user.User;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.common.SlaPolicyRepository;
import com.itsm.core.repository.company.CompanyRepository;
import com.itsm.core.repository.incident.*;
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
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("test")
class IncidentServiceTest {

    @Mock private IncidentRepository incidentRepository;
    @Mock private IncidentAssetRepository incidentAssetRepository;
    @Mock private IncidentAssigneeRepository incidentAssigneeRepository;
    @Mock private IncidentCommentRepository incidentCommentRepository;
    @Mock private IncidentHistoryRepository incidentHistoryRepository;
    @Mock private IncidentReportRepository incidentReportRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private UserRepository userRepository;
    @Mock private SlaPolicyRepository slaPolicyRepository;

    @InjectMocks
    private IncidentService incidentService;

    private Company company;
    private User manager;
    private Incident incident;

    @BeforeEach
    void setUp() {
        company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        ReflectionTestUtils.setField(company, "companyId", 1L);

        manager = User.builder().loginId("mgr01").password("pw").userNm("매니저").build();
        ReflectionTestUtils.setField(manager, "userId", 10L);

        incident = Incident.builder()
                .title("서버 장애 발생")
                .content("메인 서버 다운")
                .incidentTypeCd("SYSTEM_DOWN")
                .priorityCd("CRITICAL")
                .occurredAt(LocalDateTime.of(2026, 3, 13, 10, 0))
                .company(company)
                .build();
        ReflectionTestUtils.setField(incident, "incidentId", 1L);
    }

    @Test
    @DisplayName("장애 목록을 검색 조건으로 조회한다")
    void search_returnsList() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Incident> page = new PageImpl<>(List.of(incident), pageable, 1);
        given(incidentRepository.search(null, null, null, null, null, pageable)).willReturn(page);

        Page<IncidentResponse> result = incidentService.search(null, null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("서버 장애 발생");
    }

    @Test
    @DisplayName("장애 상세를 조회한다")
    void getDetail_returnsResponse() {
        given(incidentRepository.findById(1L)).willReturn(Optional.of(incident));

        IncidentResponse result = incidentService.getDetail(1L);

        assertThat(result.getIncidentId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("서버 장애 발생");
    }

    @Test
    @DisplayName("존재하지 않는 장애 조회 시 예외가 발생한다")
    void getDetail_notFound_throwsException() {
        given(incidentRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> incidentService.getDetail(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("장애를 성공적으로 생성한다")
    void create_success() {
        IncidentCreateRequest req = new IncidentCreateRequest(
                "서버 장애", "메인 서버 다운", "SYSTEM_DOWN", "CRITICAL",
                LocalDateTime.of(2026, 3, 13, 10, 0), 1L, null, null);

        given(companyRepository.findById(1L)).willReturn(Optional.of(company));
        SlaPolicy slaPolicy = SlaPolicy.builder()
                .priorityCd("CRITICAL").deadlineHours(4).warningPct(80).build();
        given(slaPolicyRepository.findByCompanyIdAndPriorityCd(1L, "CRITICAL")).willReturn(Optional.of(slaPolicy));
        given(incidentRepository.save(any(Incident.class))).willAnswer(inv -> {
            Incident saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "incidentId", 2L);
            return saved;
        });

        IncidentResponse result = incidentService.create(req, 1L);

        assertThat(result.getTitle()).isEqualTo("서버 장애");
        assertThat(result.getStatusCd()).isEqualTo("RECEIVED");
        assertThat(result.getSlaDeadlineAt()).isNotNull();
        verify(incidentRepository).save(any(Incident.class));
    }

    @Test
    @DisplayName("장애를 수정하면 변경 이력이 저장된다")
    void update_savesHistory() {
        IncidentUpdateRequest req = new IncidentUpdateRequest(
                "서버 장애 - 수정", "내용 수정", "NETWORK_ISSUE", "HIGH",
                LocalDateTime.of(2026, 3, 13, 11, 0), null);

        given(incidentRepository.findById(1L)).willReturn(Optional.of(incident));

        IncidentResponse result = incidentService.update(1L, req, 1L);

        assertThat(result.getTitle()).isEqualTo("서버 장애 - 수정");
        verify(incidentHistoryRepository).saveAll(any());
    }

    @Test
    @DisplayName("장애 상태를 변경한다")
    void changeStatus_success() {
        given(incidentRepository.findById(1L)).willReturn(Optional.of(incident));

        incidentService.changeStatus(1L, "IN_PROGRESS", 1L);

        assertThat(incident.getStatusCd()).isEqualTo("IN_PROGRESS");
        verify(incidentHistoryRepository).save(any(IncidentHistory.class));
    }

    @Test
    @DisplayName("유효하지 않은 상태 전이 시 예외가 발생한다")
    void changeStatus_invalidTransition_throwsException() {
        given(incidentRepository.findById(1L)).willReturn(Optional.of(incident));

        assertThatThrownBy(() -> incidentService.changeStatus(1L, "CLOSED", 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_STATE_TRANSITION);
    }

    @Test
    @DisplayName("담당자를 지정한다")
    void assignUser_success() {
        given(incidentRepository.findById(1L)).willReturn(Optional.of(incident));
        given(userRepository.findById(10L)).willReturn(Optional.of(manager));
        given(incidentAssigneeRepository.findById(any())).willReturn(Optional.empty());
        given(incidentAssigneeRepository.save(any(IncidentAssignee.class))).willAnswer(inv -> inv.getArgument(0));

        IncidentAssigneeResponse result = incidentService.assignUser(1L, 10L, 1L);

        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getUserNm()).isEqualTo("매니저");
    }

    @Test
    @DisplayName("담당자를 해제한다")
    void removeAssignee_success() {
        IncidentAssignee assignee = IncidentAssignee.builder()
                .incidentId(1L).userId(10L).grantedBy(1L).build();
        given(incidentAssigneeRepository.findById(any())).willReturn(Optional.of(assignee));

        incidentService.removeAssignee(1L, 10L);

        verify(incidentAssigneeRepository).delete(assignee);
    }

    @Test
    @DisplayName("댓글을 등록한다")
    void addComment_success() {
        given(incidentRepository.findById(1L)).willReturn(Optional.of(incident));
        given(incidentCommentRepository.save(any(IncidentComment.class))).willAnswer(inv -> {
            IncidentComment c = inv.getArgument(0);
            ReflectionTestUtils.setField(c, "commentId", 1L);
            return c;
        });

        IncidentCommentResponse result = incidentService.addComment(1L, "확인 중", 10L);

        assertThat(result.getContent()).isEqualTo("확인 중");
        assertThat(result.getCreatedBy()).isEqualTo(10L);
    }

    @Test
    @DisplayName("댓글을 수정한다")
    void updateComment_success() {
        IncidentComment comment = IncidentComment.builder()
                .incidentId(1L).content("원래 내용").createdBy(10L).build();
        ReflectionTestUtils.setField(comment, "commentId", 1L);
        given(incidentCommentRepository.findById(1L)).willReturn(Optional.of(comment));

        IncidentCommentResponse result = incidentService.updateComment(1L, 1L, "수정된 내용", 10L);

        assertThat(result.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("댓글을 삭제한다")
    void deleteComment_success() {
        IncidentComment comment = IncidentComment.builder()
                .incidentId(1L).content("내용").createdBy(10L).build();
        ReflectionTestUtils.setField(comment, "commentId", 1L);
        given(incidentCommentRepository.findById(1L)).willReturn(Optional.of(comment));

        incidentService.deleteComment(1L, 1L);

        verify(incidentCommentRepository).delete(comment);
    }

    @Test
    @DisplayName("변경 이력을 조회한다")
    void getHistory_returnsList() {
        IncidentHistory history = IncidentHistory.builder()
                .incidentId(1L).changedField("statusCd")
                .beforeValue("RECEIVED").afterValue("IN_PROGRESS")
                .createdBy(1L).build();
        ReflectionTestUtils.setField(history, "historyId", 1L);
        given(incidentHistoryRepository.findByIncidentIdOrderByCreatedAtDesc(1L)).willReturn(List.of(history));

        List<IncidentHistoryResponse> result = incidentService.getHistory(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChangedField()).isEqualTo("statusCd");
    }

    @Test
    @DisplayName("장애보고서를 작성한다")
    void saveReport_success() {
        given(incidentRepository.findById(1L)).willReturn(Optional.of(incident));
        given(incidentReportRepository.findByIncidentId(1L)).willReturn(Optional.empty());
        given(incidentReportRepository.save(any(IncidentReport.class))).willAnswer(inv -> {
            IncidentReport r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "reportId", 1L);
            return r;
        });

        IncidentReportRequest req = new IncidentReportRequest(1L, "{\"summary\":\"장애 요약\"}");
        IncidentReportResponse result = incidentService.saveReport(1L, req, 1L);

        assertThat(result.getReportContent()).isEqualTo("{\"summary\":\"장애 요약\"}");
    }

    @Test
    @DisplayName("장애보고서를 수정한다")
    void updateReport_success() {
        IncidentReport report = IncidentReport.builder()
                .incidentId(1L).reportFormId(1L).reportContent("{\"old\":\"data\"}").createdBy(1L).build();
        ReflectionTestUtils.setField(report, "reportId", 1L);
        given(incidentReportRepository.findByIncidentId(1L)).willReturn(Optional.of(report));

        IncidentReportRequest req = new IncidentReportRequest(1L, "{\"updated\":\"data\"}");
        IncidentReportResponse result = incidentService.updateReport(1L, req, 1L);

        assertThat(result.getReportContent()).isEqualTo("{\"updated\":\"data\"}");
    }

    @Test
    @DisplayName("SLA 경과율을 계산한다")
    void calculateSlaPercentage_returnsCorrectValue() {
        LocalDateTime occurredAt = LocalDateTime.now().minusHours(2);
        LocalDateTime deadline = LocalDateTime.now().plusHours(2);
        ReflectionTestUtils.setField(incident, "occurredAt", occurredAt);
        incident.setSlaDeadline(deadline);

        given(incidentRepository.findById(1L)).willReturn(Optional.of(incident));

        IncidentResponse result = incidentService.getDetail(1L);

        assertThat(result.getSlaPercentage()).isBetween(40.0, 60.0);
    }
}
