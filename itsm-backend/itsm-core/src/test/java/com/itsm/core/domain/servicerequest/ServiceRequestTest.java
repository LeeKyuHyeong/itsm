package com.itsm.core.domain.servicerequest;

import com.itsm.core.domain.company.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceRequestTest {

    private Company company;

    @BeforeEach
    void setUp() {
        company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        ReflectionTestUtils.setField(company, "companyId", 1L);
    }

    @Test
    @DisplayName("Builder로 ServiceRequest 생성 시 기본 상태는 RECEIVED이다")
    void builder_createsWithReceivedStatus() {
        ServiceRequest sr = createReceived();

        assertThat(sr.getTitle()).isEqualTo("계정 생성 요청");
        assertThat(sr.getStatusCd()).isEqualTo("RECEIVED");
        assertThat(sr.getRejectCnt()).isEqualTo(0);
    }

    @Test
    @DisplayName("RECEIVED → ASSIGNED 상태 전이가 가능하다")
    void changeStatus_receivedToAssigned() {
        ServiceRequest sr = createReceived();
        sr.changeStatus("ASSIGNED");
        assertThat(sr.getStatusCd()).isEqualTo("ASSIGNED");
    }

    @Test
    @DisplayName("ASSIGNED → IN_PROGRESS 상태 전이가 가능하다")
    void changeStatus_assignedToInProgress() {
        ServiceRequest sr = createReceived();
        sr.changeStatus("ASSIGNED");
        sr.changeStatus("IN_PROGRESS");
        assertThat(sr.getStatusCd()).isEqualTo("IN_PROGRESS");
    }

    @Test
    @DisplayName("IN_PROGRESS → PENDING_COMPLETE 상태 전이가 가능하다")
    void changeStatus_inProgressToPendingComplete() {
        ServiceRequest sr = createReceived();
        sr.changeStatus("ASSIGNED");
        sr.changeStatus("IN_PROGRESS");
        sr.changeStatus("PENDING_COMPLETE");
        assertThat(sr.getStatusCd()).isEqualTo("PENDING_COMPLETE");
    }

    @Test
    @DisplayName("PENDING_COMPLETE → CLOSED 상태 전이가 가능하다")
    void changeStatus_pendingCompleteToClosed() {
        ServiceRequest sr = createReceived();
        sr.changeStatus("ASSIGNED");
        sr.changeStatus("IN_PROGRESS");
        sr.changeStatus("PENDING_COMPLETE");
        sr.changeStatus("CLOSED");
        assertThat(sr.getStatusCd()).isEqualTo("CLOSED");
        assertThat(sr.getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("RECEIVED → CANCELLED 상태 전이가 가능하다")
    void changeStatus_receivedToCancelled() {
        ServiceRequest sr = createReceived();
        sr.changeStatus("CANCELLED");
        assertThat(sr.getStatusCd()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("RECEIVED → REJECTED 상태 전이 시 반려 횟수가 증가한다")
    void changeStatus_receivedToRejected_incrementsRejectCnt() {
        ServiceRequest sr = createReceived();
        sr.changeStatus("REJECTED");
        assertThat(sr.getStatusCd()).isEqualTo("REJECTED");
        assertThat(sr.getRejectCnt()).isEqualTo(1);
    }

    @Test
    @DisplayName("REJECTED → RECEIVED 재접수가 가능하다")
    void changeStatus_rejectedToReceived() {
        ServiceRequest sr = createReceived();
        sr.changeStatus("REJECTED");
        sr.changeStatus("RECEIVED");
        assertThat(sr.getStatusCd()).isEqualTo("RECEIVED");
        assertThat(sr.getRejectCnt()).isEqualTo(1);
    }

    @Test
    @DisplayName("유효하지 않은 상태 전이 시 예외가 발생한다")
    void changeStatus_invalidTransition_throwsException() {
        ServiceRequest sr = createReceived();
        assertThatThrownBy(() -> sr.changeStatus("CLOSED"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("유효하지 않은 상태 전이");
    }

    @Test
    @DisplayName("만족도를 제출한다")
    void submitSatisfaction_setsScore() {
        ServiceRequest sr = createReceived();
        sr.changeStatus("ASSIGNED");
        sr.changeStatus("IN_PROGRESS");
        sr.changeStatus("PENDING_COMPLETE");
        sr.changeStatus("CLOSED");

        sr.submitSatisfaction(5, "매우 만족합니다.");

        assertThat(sr.getSatisfactionScore()).isEqualTo(5);
        assertThat(sr.getSatisfactionComment()).isEqualTo("매우 만족합니다.");
        assertThat(sr.getSatisfactionSubmittedAt()).isNotNull();
    }

    @Test
    @DisplayName("update로 요청 정보를 수정한다")
    void update_changesInfo() {
        ServiceRequest sr = createReceived();
        sr.update("수정된 제목", "수정된 내용", "INSTALL", "HIGH",
                LocalDateTime.of(2026, 3, 14, 10, 0));

        assertThat(sr.getTitle()).isEqualTo("수정된 제목");
        assertThat(sr.getRequestTypeCd()).isEqualTo("INSTALL");
    }

    @Test
    @DisplayName("SLA 기한을 설정하고 연장한다")
    void slaDeadline_setAndExtend() {
        ServiceRequest sr = createReceived();
        LocalDateTime deadline = LocalDateTime.now().plusHours(8);
        sr.setSlaDeadline(deadline);
        assertThat(sr.getSlaDeadlineAt()).isEqualTo(deadline);

        sr.extendSlaDeadline(4);
        assertThat(sr.getSlaDeadlineAt()).isEqualTo(deadline.plusHours(4));
    }

    private ServiceRequest createReceived() {
        return ServiceRequest.builder()
                .title("계정 생성 요청")
                .content("신규 직원 계정 생성 요청합니다.")
                .requestTypeCd("ACCOUNT")
                .priorityCd("MEDIUM")
                .occurredAt(LocalDateTime.now())
                .company(company)
                .build();
    }
}
