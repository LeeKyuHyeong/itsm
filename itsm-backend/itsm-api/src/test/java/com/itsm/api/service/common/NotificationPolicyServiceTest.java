package com.itsm.api.service.common;

import com.itsm.api.dto.common.NotificationPolicyCreateRequest;
import com.itsm.api.dto.common.NotificationPolicyResponse;
import com.itsm.api.dto.common.NotificationPolicyUpdateRequest;
import com.itsm.core.domain.common.NotificationPolicy;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.common.NotificationPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class NotificationPolicyServiceTest {

    @Mock
    private NotificationPolicyRepository notificationPolicyRepository;

    @InjectMocks
    private NotificationPolicyService notificationPolicyService;

    private NotificationPolicy notificationPolicy;

    @BeforeEach
    void setUp() {
        notificationPolicy = NotificationPolicy.builder()
                .notiTypeCd("SLA_WARNING")
                .triggerCondition("SLA 경과율 80% 초과")
                .targetRoleCd("ROLE_PM")
                .isActive("Y")
                .createdBy(1L)
                .build();
        ReflectionTestUtils.setField(notificationPolicy, "policyId", 1L);
    }

    @Test
    @DisplayName("전체 알림 정책 목록을 조회한다")
    void getAllPolicies_returnsList() {
        // given
        given(notificationPolicyRepository.findAll()).willReturn(List.of(notificationPolicy));

        // when
        List<NotificationPolicyResponse> result = notificationPolicyService.getAllPolicies();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNotiTypeCd()).isEqualTo("SLA_WARNING");
        assertThat(result.get(0).getTriggerCondition()).isEqualTo("SLA 경과율 80% 초과");
    }

    @Test
    @DisplayName("알림 유형별 정책을 조회한다")
    void getPoliciesByType_returnsList() {
        // given
        given(notificationPolicyRepository.findByNotiTypeCd("SLA_WARNING"))
                .willReturn(List.of(notificationPolicy));

        // when
        List<NotificationPolicyResponse> result = notificationPolicyService.getPoliciesByType("SLA_WARNING");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNotiTypeCd()).isEqualTo("SLA_WARNING");
    }

    @Test
    @DisplayName("알림 정책을 성공적으로 생성한다")
    void createPolicy_success() {
        // given
        NotificationPolicyCreateRequest req = new NotificationPolicyCreateRequest(
                "INCIDENT_ASSIGN", "장애 담당자 배정 시", "ROLE_ENGINEER");
        given(notificationPolicyRepository.save(any(NotificationPolicy.class))).willAnswer(invocation -> {
            NotificationPolicy saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "policyId", 2L);
            return saved;
        });

        // when
        NotificationPolicyResponse result = notificationPolicyService.createPolicy(req, 1L);

        // then
        assertThat(result.getNotiTypeCd()).isEqualTo("INCIDENT_ASSIGN");
        assertThat(result.getTriggerCondition()).isEqualTo("장애 담당자 배정 시");
        assertThat(result.getTargetRoleCd()).isEqualTo("ROLE_ENGINEER");
        verify(notificationPolicyRepository).save(any(NotificationPolicy.class));
    }

    @Test
    @DisplayName("알림 정책을 수정한다")
    void updatePolicy_success() {
        // given
        NotificationPolicyUpdateRequest req = new NotificationPolicyUpdateRequest(
                "SLA 경과율 90% 초과", "ROLE_ADMIN");
        given(notificationPolicyRepository.findById(1L)).willReturn(Optional.of(notificationPolicy));

        // when
        NotificationPolicyResponse result = notificationPolicyService.updatePolicy(1L, req, 1L);

        // then
        assertThat(result.getTriggerCondition()).isEqualTo("SLA 경과율 90% 초과");
        assertThat(result.getTargetRoleCd()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("존재하지 않는 알림 정책을 수정하면 예외가 발생한다")
    void updatePolicy_notFound_throwsException() {
        // given
        NotificationPolicyUpdateRequest req = new NotificationPolicyUpdateRequest(
                "조건 변경", "ROLE_PM");
        given(notificationPolicyRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationPolicyService.updatePolicy(999L, req, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("알림 정책 상태를 변경한다")
    void changePolicyStatus_success() {
        // given
        given(notificationPolicyRepository.findById(1L)).willReturn(Optional.of(notificationPolicy));

        // when
        notificationPolicyService.changePolicyStatus(1L, "N");

        // then
        assertThat(notificationPolicy.getIsActive()).isEqualTo("N");
    }
}
