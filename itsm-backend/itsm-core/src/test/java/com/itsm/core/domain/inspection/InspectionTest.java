package com.itsm.core.domain.inspection;

import com.itsm.core.domain.company.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InspectionTest {

    private Company company;
    private Inspection inspection;

    @BeforeEach
    void setUp() {
        company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        ReflectionTestUtils.setField(company, "companyId", 1L);

        inspection = Inspection.builder()
                .title("서버 정기점검")
                .inspectionTypeCd("MONTHLY")
                .scheduledAt(LocalDate.of(2026, 3, 20))
                .company(company)
                .description("월별 정기점검")
                .build();
    }

    @Test
    @DisplayName("점검 생성 시 초기 상태는 SCHEDULED이다")
    void create_initialStatusIsScheduled() {
        assertThat(inspection.getStatusCd()).isEqualTo("SCHEDULED");
        assertThat(inspection.getTitle()).isEqualTo("서버 정기점검");
        assertThat(inspection.getDescription()).isEqualTo("월별 정기점검");
    }

    @Test
    @DisplayName("SCHEDULED → IN_PROGRESS 전이가 가능하다")
    void changeStatus_scheduledToInProgress() {
        inspection.changeStatus("IN_PROGRESS");
        assertThat(inspection.getStatusCd()).isEqualTo("IN_PROGRESS");
    }

    @Test
    @DisplayName("SCHEDULED → ON_HOLD 전이가 가능하다")
    void changeStatus_scheduledToOnHold() {
        inspection.changeStatus("ON_HOLD");
        assertThat(inspection.getStatusCd()).isEqualTo("ON_HOLD");
    }

    @Test
    @DisplayName("IN_PROGRESS → COMPLETED 전이가 가능하다")
    void changeStatus_inProgressToCompleted() {
        inspection.changeStatus("IN_PROGRESS");
        inspection.changeStatus("COMPLETED");
        assertThat(inspection.getStatusCd()).isEqualTo("COMPLETED");
        assertThat(inspection.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("ON_HOLD → SCHEDULED, IN_PROGRESS 전이가 가능하다")
    void changeStatus_onHoldTransitions() {
        inspection.changeStatus("ON_HOLD");
        inspection.changeStatus("SCHEDULED");
        assertThat(inspection.getStatusCd()).isEqualTo("SCHEDULED");

        inspection.changeStatus("ON_HOLD");
        inspection.changeStatus("IN_PROGRESS");
        assertThat(inspection.getStatusCd()).isEqualTo("IN_PROGRESS");
    }

    @Test
    @DisplayName("COMPLETED → CLOSED 전이가 가능하다")
    void changeStatus_completedToClosed() {
        inspection.changeStatus("IN_PROGRESS");
        inspection.changeStatus("COMPLETED");
        inspection.changeStatus("CLOSED");
        assertThat(inspection.getStatusCd()).isEqualTo("CLOSED");
        assertThat(inspection.getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("CLOSED에서는 더 이상 전이가 불가하다")
    void changeStatus_closedNoTransition() {
        inspection.changeStatus("IN_PROGRESS");
        inspection.changeStatus("COMPLETED");
        inspection.changeStatus("CLOSED");

        assertThatThrownBy(() -> inspection.changeStatus("SCHEDULED"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("유효하지 않은 상태 전이");
    }

    @Test
    @DisplayName("유효하지 않은 상태 전이 시 예외가 발생한다")
    void changeStatus_invalidTransition_throwsException() {
        assertThatThrownBy(() -> inspection.changeStatus("CLOSED"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("유효하지 않은 상태 전이");
    }

    @Test
    @DisplayName("점검을 수정할 수 있다")
    void update_changesFields() {
        inspection.update("네트워크 점검", "WEEKLY", LocalDate.of(2026, 4, 1), "주별 점검");
        assertThat(inspection.getTitle()).isEqualTo("네트워크 점검");
        assertThat(inspection.getInspectionTypeCd()).isEqualTo("WEEKLY");
        assertThat(inspection.getScheduledAt()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(inspection.getDescription()).isEqualTo("주별 점검");
    }

    @Test
    @DisplayName("점검항목을 생성할 수 있다")
    void inspectionItem_create() {
        InspectionItem item = InspectionItem.builder()
                .inspectionId(1L)
                .itemNm("CPU 사용률 점검")
                .sortOrder(1)
                .isRequired("Y")
                .build();
        assertThat(item.getItemNm()).isEqualTo("CPU 사용률 점검");
        assertThat(item.getIsRequired()).isEqualTo("Y");
    }

    @Test
    @DisplayName("점검결과를 생성할 수 있다")
    void inspectionResult_create() {
        InspectionResult result = InspectionResult.builder()
                .inspectionId(1L)
                .itemId(1L)
                .resultValue("정상")
                .isNormal("Y")
                .remark("이상 없음")
                .build();
        assertThat(result.getResultValue()).isEqualTo("정상");
        assertThat(result.getIsNormal()).isEqualTo("Y");
    }

    @Test
    @DisplayName("점검결과를 수정할 수 있다")
    void inspectionResult_update() {
        InspectionResult result = InspectionResult.builder()
                .inspectionId(1L)
                .itemId(1L)
                .resultValue("정상")
                .isNormal("Y")
                .build();
        result.update("비정상", "N", "CPU 사용률 90% 초과");
        assertThat(result.getResultValue()).isEqualTo("비정상");
        assertThat(result.getIsNormal()).isEqualTo("N");
        assertThat(result.getRemark()).isEqualTo("CPU 사용률 90% 초과");
    }
}
