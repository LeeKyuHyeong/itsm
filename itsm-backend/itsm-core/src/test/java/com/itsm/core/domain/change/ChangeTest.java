package com.itsm.core.domain.change;

import com.itsm.core.domain.company.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChangeTest {

    private Company company;
    private Change change;

    @BeforeEach
    void setUp() {
        company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        ReflectionTestUtils.setField(company, "companyId", 1L);

        change = Change.builder()
                .title("서버 패치 적용")
                .content("보안 패치 적용")
                .changeTypeCd("NORMAL")
                .priorityCd("MEDIUM")
                .scheduledAt(LocalDateTime.of(2026, 3, 20, 10, 0))
                .rollbackPlan("이전 버전 복원")
                .company(company)
                .build();
    }

    @Test
    @DisplayName("변경요청 생성 시 초기 상태는 DRAFT이다")
    void create_initialStatusIsDraft() {
        assertThat(change.getStatusCd()).isEqualTo("DRAFT");
        assertThat(change.getTitle()).isEqualTo("서버 패치 적용");
        assertThat(change.getRollbackPlan()).isEqualTo("이전 버전 복원");
    }

    @Test
    @DisplayName("DRAFT → APPROVAL_REQUESTED 전이가 가능하다")
    void changeStatus_draftToApprovalRequested() {
        change.changeStatus("APPROVAL_REQUESTED");
        assertThat(change.getStatusCd()).isEqualTo("APPROVAL_REQUESTED");
    }

    @Test
    @DisplayName("APPROVAL_REQUESTED → APPROVED → IN_PROGRESS → COMPLETED → CLOSED 전이가 가능하다")
    void changeStatus_fullFlow() {
        change.changeStatus("APPROVAL_REQUESTED");
        change.changeStatus("APPROVED");
        change.changeStatus("IN_PROGRESS");
        change.changeStatus("COMPLETED");
        change.changeStatus("CLOSED");
        assertThat(change.getStatusCd()).isEqualTo("CLOSED");
    }

    @Test
    @DisplayName("APPROVAL_REQUESTED → REJECTED → DRAFT 전이가 가능하다")
    void changeStatus_rejectedFlow() {
        change.changeStatus("APPROVAL_REQUESTED");
        change.changeStatus("REJECTED");
        assertThat(change.getStatusCd()).isEqualTo("REJECTED");

        change.changeStatus("DRAFT");
        assertThat(change.getStatusCd()).isEqualTo("DRAFT");
    }

    @Test
    @DisplayName("유효하지 않은 상태 전이 시 예외가 발생한다")
    void changeStatus_invalidTransition_throwsException() {
        assertThatThrownBy(() -> change.changeStatus("CLOSED"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("유효하지 않은 상태 전이");
    }

    @Test
    @DisplayName("변경요청을 수정할 수 있다")
    void update_changesFields() {
        change.update("패치 적용 수정", "내용 수정", "EMERGENCY", "HIGH",
                LocalDateTime.of(2026, 3, 21, 10, 0), "수정된 롤백 계획");
        assertThat(change.getTitle()).isEqualTo("패치 적용 수정");
        assertThat(change.getChangeTypeCd()).isEqualTo("EMERGENCY");
        assertThat(change.getRollbackPlan()).isEqualTo("수정된 롤백 계획");
    }

    @Test
    @DisplayName("승인자 승인/반려 처리가 가능하다")
    void approver_approveAndReject() {
        ChangeApprover approver = ChangeApprover.builder()
                .changeId(1L).userId(10L).approveOrder(1).build();
        assertThat(approver.getApproveStatus()).isEqualTo("PENDING");

        approver.approve("승인합니다");
        assertThat(approver.getApproveStatus()).isEqualTo("APPROVED");
        assertThat(approver.getComment()).isEqualTo("승인합니다");
        assertThat(approver.getApprovedAt()).isNotNull();
    }
}
