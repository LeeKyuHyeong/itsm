package com.itsm.api.service.common;

import com.itsm.api.dto.common.SlaPolicyCreateRequest;
import com.itsm.api.dto.common.SlaPolicyResponse;
import com.itsm.api.dto.common.SlaPolicyUpdateRequest;
import com.itsm.core.domain.common.SlaPolicy;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.common.SlaPolicyRepository;
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
class SlaPolicyServiceTest {

    @Mock
    private SlaPolicyRepository slaPolicyRepository;

    @InjectMocks
    private SlaPolicyService slaPolicyService;

    private SlaPolicy slaPolicy;

    @BeforeEach
    void setUp() {
        slaPolicy = SlaPolicy.builder()
                .companyId(null)
                .priorityCd("CRITICAL")
                .deadlineHours(4)
                .warningPct(80)
                .isActive("Y")
                .createdBy(1L)
                .build();
        ReflectionTestUtils.setField(slaPolicy, "policyId", 1L);
    }

    @Test
    @DisplayName("전체 SLA 정책 목록을 조회한다")
    void getAllPolicies_returnsList() {
        // given
        given(slaPolicyRepository.findAll()).willReturn(List.of(slaPolicy));

        // when
        List<SlaPolicyResponse> result = slaPolicyService.getAllPolicies();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPriorityCd()).isEqualTo("CRITICAL");
        assertThat(result.get(0).getDeadlineHours()).isEqualTo(4);
    }

    @Test
    @DisplayName("회사별 SLA 정책을 조회한다 - companyId가 null이면 기본값 조회")
    void getPoliciesByCompany_nullCompanyId_returnsDefaults() {
        // given
        given(slaPolicyRepository.findByCompanyIdIsNull()).willReturn(List.of(slaPolicy));

        // when
        List<SlaPolicyResponse> result = slaPolicyService.getPoliciesByCompany(null);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCompanyId()).isNull();
    }

    @Test
    @DisplayName("회사별 SLA 정책을 조회한다 - companyId가 있으면 해당 회사 조회")
    void getPoliciesByCompany_withCompanyId_returnsCompanyPolicies() {
        // given
        SlaPolicy companyPolicy = SlaPolicy.builder()
                .companyId(10L)
                .priorityCd("HIGH")
                .deadlineHours(8)
                .warningPct(70)
                .isActive("Y")
                .createdBy(1L)
                .build();
        ReflectionTestUtils.setField(companyPolicy, "policyId", 2L);
        given(slaPolicyRepository.findByCompanyId(10L)).willReturn(List.of(companyPolicy));

        // when
        List<SlaPolicyResponse> result = slaPolicyService.getPoliciesByCompany(10L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCompanyId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("SLA 정책을 성공적으로 생성한다")
    void createPolicy_success() {
        // given
        SlaPolicyCreateRequest req = new SlaPolicyCreateRequest(null, "HIGH", 8, 70);
        given(slaPolicyRepository.save(any(SlaPolicy.class))).willAnswer(invocation -> {
            SlaPolicy saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "policyId", 2L);
            return saved;
        });

        // when
        SlaPolicyResponse result = slaPolicyService.createPolicy(req, 1L);

        // then
        assertThat(result.getPriorityCd()).isEqualTo("HIGH");
        assertThat(result.getDeadlineHours()).isEqualTo(8);
        assertThat(result.getWarningPct()).isEqualTo(70);
        verify(slaPolicyRepository).save(any(SlaPolicy.class));
    }

    @Test
    @DisplayName("SLA 정책을 수정한다")
    void updatePolicy_success() {
        // given
        SlaPolicyUpdateRequest req = new SlaPolicyUpdateRequest(12, 90);
        given(slaPolicyRepository.findById(1L)).willReturn(Optional.of(slaPolicy));

        // when
        SlaPolicyResponse result = slaPolicyService.updatePolicy(1L, req, 1L);

        // then
        assertThat(result.getDeadlineHours()).isEqualTo(12);
        assertThat(result.getWarningPct()).isEqualTo(90);
    }

    @Test
    @DisplayName("존재하지 않는 SLA 정책을 수정하면 예외가 발생한다")
    void updatePolicy_notFound_throwsException() {
        // given
        SlaPolicyUpdateRequest req = new SlaPolicyUpdateRequest(12, 90);
        given(slaPolicyRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> slaPolicyService.updatePolicy(999L, req, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("SLA 정책 상태를 변경한다")
    void changePolicyStatus_success() {
        // given
        given(slaPolicyRepository.findById(1L)).willReturn(Optional.of(slaPolicy));

        // when
        slaPolicyService.changePolicyStatus(1L, "N");

        // then
        assertThat(slaPolicy.getIsActive()).isEqualTo("N");
    }
}
