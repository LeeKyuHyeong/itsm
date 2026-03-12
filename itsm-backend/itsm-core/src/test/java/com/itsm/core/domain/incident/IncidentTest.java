package com.itsm.core.domain.incident;

import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IncidentTest {

    private Company company;
    private User creator;

    @BeforeEach
    void setUp() {
        company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        ReflectionTestUtils.setField(company, "companyId", 1L);

        creator = User.builder().loginId("user01").password("pw").userNm("등록자").build();
        ReflectionTestUtils.setField(creator, "userId", 1L);
    }

    @Test
    @DisplayName("Builder로 Incident 생성 시 기본 상태는 RECEIVED이다")
    void builder_createsIncidentWithReceivedStatus() {
        Incident incident = Incident.builder()
                .title("서버 장애 발생")
                .content("메인 서버 다운")
                .incidentTypeCd("SYSTEM_DOWN")
                .priorityCd("CRITICAL")
                .occurredAt(LocalDateTime.now())
                .company(company)
                .build();

        assertThat(incident.getTitle()).isEqualTo("서버 장애 발생");
        assertThat(incident.getContent()).isEqualTo("메인 서버 다운");
        assertThat(incident.getIncidentTypeCd()).isEqualTo("SYSTEM_DOWN");
        assertThat(incident.getPriorityCd()).isEqualTo("CRITICAL");
        assertThat(incident.getStatusCd()).isEqualTo("RECEIVED");
        assertThat(incident.getCompany()).isNotNull();
    }

    @Test
    @DisplayName("RECEIVED → IN_PROGRESS 상태 전이가 가능하다")
    void changeStatus_receivedToInProgress_succeeds() {
        Incident incident = createReceivedIncident();
        incident.changeStatus("IN_PROGRESS");
        assertThat(incident.getStatusCd()).isEqualTo("IN_PROGRESS");
    }

    @Test
    @DisplayName("IN_PROGRESS → COMPLETED 상태 전이가 가능하다")
    void changeStatus_inProgressToCompleted_succeeds() {
        Incident incident = createReceivedIncident();
        incident.changeStatus("IN_PROGRESS");
        incident.changeStatus("COMPLETED");
        assertThat(incident.getStatusCd()).isEqualTo("COMPLETED");
        assertThat(incident.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("COMPLETED → CLOSED 상태 전이가 가능하다")
    void changeStatus_completedToClosed_succeeds() {
        Incident incident = createReceivedIncident();
        incident.changeStatus("IN_PROGRESS");
        incident.changeStatus("COMPLETED");
        incident.changeStatus("CLOSED");
        assertThat(incident.getStatusCd()).isEqualTo("CLOSED");
        assertThat(incident.getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("RECEIVED → REJECTED 상태 전이가 가능하다")
    void changeStatus_receivedToRejected_succeeds() {
        Incident incident = createReceivedIncident();
        incident.changeStatus("REJECTED");
        assertThat(incident.getStatusCd()).isEqualTo("REJECTED");
    }

    @Test
    @DisplayName("REJECTED → RECEIVED 상태 전이가 가능하다 (재접수)")
    void changeStatus_rejectedToReceived_succeeds() {
        Incident incident = createReceivedIncident();
        incident.changeStatus("REJECTED");
        incident.changeStatus("RECEIVED");
        assertThat(incident.getStatusCd()).isEqualTo("RECEIVED");
    }

    @Test
    @DisplayName("유효하지 않은 상태 전이 시 예외가 발생한다")
    void changeStatus_invalidTransition_throwsException() {
        Incident incident = createReceivedIncident();
        assertThatThrownBy(() -> incident.changeStatus("CLOSED"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("유효하지 않은 상태 전이");
    }

    @Test
    @DisplayName("COMPLETED에서 IN_PROGRESS로 되돌릴 수 없다")
    void changeStatus_completedToInProgress_throwsException() {
        Incident incident = createReceivedIncident();
        incident.changeStatus("IN_PROGRESS");
        incident.changeStatus("COMPLETED");
        assertThatThrownBy(() -> incident.changeStatus("IN_PROGRESS"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("주담당자를 지정한다")
    void assignMainManager_setsMainManager() {
        Incident incident = createReceivedIncident();
        User manager = User.builder().loginId("mgr01").password("pw").userNm("매니저").build();
        ReflectionTestUtils.setField(manager, "userId", 10L);

        incident.assignMainManager(manager);

        assertThat(incident.getMainManager()).isEqualTo(manager);
    }

    @Test
    @DisplayName("처리내용을 작성한다")
    void writeProcessContent_setsContent() {
        Incident incident = createReceivedIncident();
        incident.changeStatus("IN_PROGRESS");
        incident.writeProcessContent("서버 재기동으로 해결");

        assertThat(incident.getProcessContent()).isEqualTo("서버 재기동으로 해결");
    }

    @Test
    @DisplayName("update로 장애 정보를 수정한다")
    void update_changesIncidentInfo() {
        Incident incident = createReceivedIncident();

        incident.update("서버 장애 - 수정", "내용 수정", "NETWORK_ISSUE", "HIGH",
                LocalDateTime.of(2026, 3, 13, 10, 0));

        assertThat(incident.getTitle()).isEqualTo("서버 장애 - 수정");
        assertThat(incident.getContent()).isEqualTo("내용 수정");
        assertThat(incident.getIncidentTypeCd()).isEqualTo("NETWORK_ISSUE");
        assertThat(incident.getPriorityCd()).isEqualTo("HIGH");
    }

    @Test
    @DisplayName("SLA 기한을 설정한다")
    void setSlaDeadline_setsDeadline() {
        Incident incident = createReceivedIncident();
        LocalDateTime deadline = LocalDateTime.now().plusHours(4);
        incident.setSlaDeadline(deadline);

        assertThat(incident.getSlaDeadlineAt()).isEqualTo(deadline);
    }

    @Test
    @DisplayName("SLA 기한을 연장한다")
    void extendSlaDeadline_extendsDeadline() {
        Incident incident = createReceivedIncident();
        LocalDateTime deadline = LocalDateTime.now().plusHours(4);
        incident.setSlaDeadline(deadline);

        incident.extendSlaDeadline(2);
        assertThat(incident.getSlaDeadlineAt()).isEqualTo(deadline.plusHours(2));
    }

    private Incident createReceivedIncident() {
        return Incident.builder()
                .title("서버 장애 발생")
                .content("메인 서버 다운")
                .incidentTypeCd("SYSTEM_DOWN")
                .priorityCd("CRITICAL")
                .occurredAt(LocalDateTime.now())
                .company(company)
                .build();
    }
}
