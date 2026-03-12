package com.itsm.api.service.change;

import com.itsm.api.dto.change.*;
import com.itsm.core.domain.change.*;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.user.User;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.change.*;
import com.itsm.core.repository.company.CompanyRepository;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChangeServiceTest {

    @Mock private ChangeRepository changeRepository;
    @Mock private ChangeAssetRepository changeAssetRepository;
    @Mock private ChangeApproverRepository changeApproverRepository;
    @Mock private ChangeHistoryRepository changeHistoryRepository;
    @Mock private ChangeCommentRepository changeCommentRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ChangeService changeService;

    private Company company;
    private User user;
    private Change change;

    @BeforeEach
    void setUp() {
        company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        ReflectionTestUtils.setField(company, "companyId", 1L);

        user = User.builder().loginId("user01").password("pw").userNm("테스트유저").build();
        ReflectionTestUtils.setField(user, "userId", 10L);

        change = Change.builder()
                .title("서버 패치 적용")
                .content("보안 패치 적용")
                .changeTypeCd("NORMAL")
                .priorityCd("MEDIUM")
                .scheduledAt(LocalDateTime.of(2026, 3, 20, 10, 0))
                .rollbackPlan("이전 버전 복원")
                .company(company)
                .build();
        ReflectionTestUtils.setField(change, "changeId", 1L);
    }

    @Test
    @DisplayName("변경요청 목록을 검색 조건으로 조회한다")
    void search_returnsList() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Change> page = new PageImpl<>(List.of(change), pageable, 1);
        given(changeRepository.search(null, null, null, null, null, pageable)).willReturn(page);

        Page<ChangeResponse> result = changeService.search(null, null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("서버 패치 적용");
    }

    @Test
    @DisplayName("변경요청 상세를 조회한다")
    void getDetail_returnsResponse() {
        given(changeRepository.findById(1L)).willReturn(Optional.of(change));
        given(changeApproverRepository.findByChangeIdOrderByApproveOrderAsc(1L)).willReturn(List.of());

        ChangeResponse result = changeService.getDetail(1L);

        assertThat(result.getChangeId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("서버 패치 적용");
    }

    @Test
    @DisplayName("존재하지 않는 변경요청 조회 시 예외가 발생한다")
    void getDetail_notFound_throwsException() {
        given(changeRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> changeService.getDetail(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("변경요청을 성공적으로 생성한다")
    void create_success() {
        ChangeCreateRequest req = new ChangeCreateRequest(
                "서버 패치 적용", "보안 패치 적용", "NORMAL", "MEDIUM",
                LocalDateTime.of(2026, 3, 20, 10, 0), "이전 버전 복원", 1L);

        given(companyRepository.findById(1L)).willReturn(Optional.of(company));
        given(changeRepository.save(any(Change.class))).willAnswer(inv -> {
            Change saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "changeId", 2L);
            return saved;
        });

        ChangeResponse result = changeService.create(req, 1L);

        assertThat(result.getTitle()).isEqualTo("서버 패치 적용");
        assertThat(result.getStatusCd()).isEqualTo("DRAFT");
        verify(changeRepository).save(any(Change.class));
    }

    @Test
    @DisplayName("변경요청을 수정하면 변경 이력이 저장된다")
    void update_savesHistory() {
        ChangeUpdateRequest req = new ChangeUpdateRequest(
                "패치 적용 수정", "내용 수정", "EMERGENCY", "HIGH",
                LocalDateTime.of(2026, 3, 21, 10, 0), "수정된 롤백 계획");

        given(changeRepository.findById(1L)).willReturn(Optional.of(change));

        ChangeResponse result = changeService.update(1L, req, 1L);

        assertThat(result.getTitle()).isEqualTo("패치 적용 수정");
        verify(changeHistoryRepository).saveAll(any());
    }

    @Test
    @DisplayName("변경요청 상태를 변경한다")
    void changeStatus_success() {
        given(changeRepository.findById(1L)).willReturn(Optional.of(change));

        changeService.changeStatus(1L, "APPROVAL_REQUESTED", 1L);

        assertThat(change.getStatusCd()).isEqualTo("APPROVAL_REQUESTED");
        verify(changeHistoryRepository).save(any(ChangeHistory.class));
    }

    @Test
    @DisplayName("유효하지 않은 상태 전이 시 예외가 발생한다")
    void changeStatus_invalidTransition_throwsException() {
        given(changeRepository.findById(1L)).willReturn(Optional.of(change));

        assertThatThrownBy(() -> changeService.changeStatus(1L, "CLOSED", 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_STATE_TRANSITION);
    }

    @Test
    @DisplayName("승인자를 추가한다")
    void addApprover_success() {
        given(changeRepository.findById(1L)).willReturn(Optional.of(change));
        given(userRepository.findById(10L)).willReturn(Optional.of(user));
        given(changeApproverRepository.findById(any())).willReturn(Optional.empty());
        given(changeApproverRepository.findByChangeIdOrderByApproveOrderAsc(1L)).willReturn(List.of());
        given(changeApproverRepository.save(any(ChangeApprover.class))).willAnswer(inv -> inv.getArgument(0));

        ChangeApproverResponse result = changeService.addApprover(1L, 10L, 1L);

        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getUserNm()).isEqualTo("테스트유저");
        assertThat(result.getApproveOrder()).isEqualTo(1);
        assertThat(result.getApproveStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("이미 지정된 승인자 재추가 시 예외가 발생한다")
    void addApprover_duplicate_throwsException() {
        ChangeApprover existing = ChangeApprover.builder()
                .changeId(1L).userId(10L).approveOrder(1).build();
        given(changeRepository.findById(1L)).willReturn(Optional.of(change));
        given(userRepository.findById(10L)).willReturn(Optional.of(user));
        given(changeApproverRepository.findById(any())).willReturn(Optional.of(existing));

        assertThatThrownBy(() -> changeService.addApprover(1L, 10L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_VALUE);
    }

    @Test
    @DisplayName("순차 승인 - 현재 순서가 아닌 승인자가 승인 시 예외가 발생한다")
    void approveChange_wrongOrder_throwsException() {
        change.changeStatus("APPROVAL_REQUESTED");

        ChangeApprover approver1 = ChangeApprover.builder().changeId(1L).userId(10L).approveOrder(1).build();
        ChangeApprover approver2 = ChangeApprover.builder().changeId(1L).userId(20L).approveOrder(2).build();

        given(changeRepository.findById(1L)).willReturn(Optional.of(change));
        given(changeApproverRepository.findByChangeIdOrderByApproveOrderAsc(1L))
                .willReturn(List.of(approver1, approver2));

        assertThatThrownBy(() -> changeService.approveChange(1L, 20L, "APPROVED", "승인", 20L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("순차 승인 - 올바른 순서의 승인자가 승인한다")
    void approveChange_correctOrder_success() {
        change.changeStatus("APPROVAL_REQUESTED");

        ChangeApprover approver1 = ChangeApprover.builder().changeId(1L).userId(10L).approveOrder(1).build();

        given(changeRepository.findById(1L)).willReturn(Optional.of(change));
        given(changeApproverRepository.findByChangeIdOrderByApproveOrderAsc(1L))
                .willReturn(List.of(approver1));

        changeService.approveChange(1L, 10L, "APPROVED", "승인합니다", 10L);

        assertThat(approver1.getApproveStatus()).isEqualTo("APPROVED");
        assertThat(change.getStatusCd()).isEqualTo("APPROVED");
    }

    @Test
    @DisplayName("승인자가 반려하면 변경요청이 REJECTED로 전이된다")
    void approveChange_rejected_changesStatus() {
        change.changeStatus("APPROVAL_REQUESTED");

        ChangeApprover approver = ChangeApprover.builder().changeId(1L).userId(10L).approveOrder(1).build();

        given(changeRepository.findById(1L)).willReturn(Optional.of(change));
        given(changeApproverRepository.findByChangeIdOrderByApproveOrderAsc(1L))
                .willReturn(List.of(approver));

        changeService.approveChange(1L, 10L, "REJECTED", "반려합니다", 10L);

        assertThat(approver.getApproveStatus()).isEqualTo("REJECTED");
        assertThat(change.getStatusCd()).isEqualTo("REJECTED");
    }

    @Test
    @DisplayName("댓글을 등록한다")
    void addComment_success() {
        given(changeRepository.findById(1L)).willReturn(Optional.of(change));
        given(changeCommentRepository.save(any(ChangeComment.class))).willAnswer(inv -> {
            ChangeComment c = inv.getArgument(0);
            ReflectionTestUtils.setField(c, "commentId", 1L);
            return c;
        });

        ChangeCommentResponse result = changeService.addComment(1L, "확인 중", 10L);

        assertThat(result.getContent()).isEqualTo("확인 중");
        assertThat(result.getCreatedBy()).isEqualTo(10L);
    }

    @Test
    @DisplayName("댓글을 삭제한다")
    void deleteComment_success() {
        ChangeComment comment = ChangeComment.builder()
                .changeId(1L).content("내용").createdBy(10L).build();
        ReflectionTestUtils.setField(comment, "commentId", 1L);
        given(changeCommentRepository.findById(1L)).willReturn(Optional.of(comment));

        changeService.deleteComment(1L, 1L);

        verify(changeCommentRepository).delete(comment);
    }

    @Test
    @DisplayName("변경 이력을 조회한다")
    void getHistory_returnsList() {
        ChangeHistory history = ChangeHistory.builder()
                .changeId(1L).changedField("statusCd")
                .beforeValue("DRAFT").afterValue("APPROVAL_REQUESTED")
                .createdBy(1L).build();
        ReflectionTestUtils.setField(history, "historyId", 1L);
        given(changeHistoryRepository.findByChangeIdOrderByCreatedAtDesc(1L)).willReturn(List.of(history));

        List<ChangeHistoryResponse> result = changeService.getHistory(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChangedField()).isEqualTo("statusCd");
    }

    @Test
    @DisplayName("전원 승인 시 자동으로 APPROVED 상태로 전이된다")
    void approveChange_allApproved_autoTransition() {
        change.changeStatus("APPROVAL_REQUESTED");

        ChangeApprover approver1 = ChangeApprover.builder().changeId(1L).userId(10L).approveOrder(1).build();
        approver1.approve("1차 승인");
        ChangeApprover approver2 = ChangeApprover.builder().changeId(1L).userId(20L).approveOrder(2).build();

        given(changeRepository.findById(1L)).willReturn(Optional.of(change));
        given(changeApproverRepository.findByChangeIdOrderByApproveOrderAsc(1L))
                .willReturn(List.of(approver1, approver2));

        changeService.approveChange(1L, 20L, "APPROVED", "2차 승인", 20L);

        assertThat(approver2.getApproveStatus()).isEqualTo("APPROVED");
        assertThat(change.getStatusCd()).isEqualTo("APPROVED");
    }
}
