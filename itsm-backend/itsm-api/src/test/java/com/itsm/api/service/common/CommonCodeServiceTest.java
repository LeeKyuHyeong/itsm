package com.itsm.api.service.common;

import com.itsm.api.dto.common.*;
import com.itsm.core.domain.common.CommonCode;
import com.itsm.core.domain.common.CommonCodeDetail;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.common.CommonCodeDetailRepository;
import com.itsm.core.repository.common.CommonCodeRepository;
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
class CommonCodeServiceTest {

    @Mock
    private CommonCodeRepository commonCodeRepository;

    @Mock
    private CommonCodeDetailRepository commonCodeDetailRepository;

    @InjectMocks
    private CommonCodeService commonCodeService;

    private CommonCode commonCode;
    private CommonCodeDetail commonCodeDetail;

    @BeforeEach
    void setUp() {
        commonCode = CommonCode.builder()
                .groupNm("우선순위")
                .groupCd("PRIORITY")
                .description("우선순위 코드")
                .isActive("Y")
                .createdBy(1L)
                .build();
        ReflectionTestUtils.setField(commonCode, "groupId", 1L);

        commonCodeDetail = CommonCodeDetail.builder()
                .commonCode(commonCode)
                .codeVal("HIGH")
                .codeNm("높음")
                .sortOrder(1)
                .isActive("Y")
                .createdBy(1L)
                .build();
        ReflectionTestUtils.setField(commonCodeDetail, "detailId", 1L);
    }

    @Test
    @DisplayName("공통코드 그룹 목록을 조회한다")
    void getGroups_returnsListOfGroups() {
        // given
        given(commonCodeRepository.findAll()).willReturn(List.of(commonCode));

        // when
        List<CommonCodeGroupResponse> result = commonCodeService.getGroups();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGroupNm()).isEqualTo("우선순위");
        assertThat(result.get(0).getGroupCd()).isEqualTo("PRIORITY");
    }

    @Test
    @DisplayName("공통코드 그룹을 성공적으로 생성한다")
    void createGroup_success() {
        // given
        CommonCodeGroupCreateRequest req = new CommonCodeGroupCreateRequest(
                "상태", "STATUS", "상태 코드");
        given(commonCodeRepository.existsByGroupCd("STATUS")).willReturn(false);
        given(commonCodeRepository.save(any(CommonCode.class))).willAnswer(invocation -> {
            CommonCode saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "groupId", 2L);
            return saved;
        });

        // when
        CommonCodeGroupResponse result = commonCodeService.createGroup(req, 1L);

        // then
        assertThat(result.getGroupNm()).isEqualTo("상태");
        assertThat(result.getGroupCd()).isEqualTo("STATUS");
        verify(commonCodeRepository).save(any(CommonCode.class));
    }

    @Test
    @DisplayName("중복 그룹코드로 생성 시 예외가 발생한다")
    void createGroup_duplicateGroupCd_throwsException() {
        // given
        CommonCodeGroupCreateRequest req = new CommonCodeGroupCreateRequest(
                "우선순위", "PRIORITY", "중복 코드");
        given(commonCodeRepository.existsByGroupCd("PRIORITY")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> commonCodeService.createGroup(req, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_VALUE);
    }

    @Test
    @DisplayName("공통코드 그룹을 성공적으로 수정한다")
    void updateGroup_success() {
        // given
        CommonCodeGroupUpdateRequest req = new CommonCodeGroupUpdateRequest(
                "우선순위(수정)", "수정된 설명");
        given(commonCodeRepository.findById(1L)).willReturn(Optional.of(commonCode));

        // when
        CommonCodeGroupResponse result = commonCodeService.updateGroup(1L, req, 1L);

        // then
        assertThat(result.getGroupNm()).isEqualTo("우선순위(수정)");
        assertThat(result.getDescription()).isEqualTo("수정된 설명");
    }

    @Test
    @DisplayName("공통코드 상세 목록을 조회한다")
    void getDetails_returnsListForGroup() {
        // given
        given(commonCodeRepository.findById(1L)).willReturn(Optional.of(commonCode));
        given(commonCodeDetailRepository.findByCommonCode_GroupIdOrderBySortOrder(1L))
                .willReturn(List.of(commonCodeDetail));

        // when
        List<CommonCodeDetailResponse> result = commonCodeService.getDetails(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCodeVal()).isEqualTo("HIGH");
        assertThat(result.get(0).getCodeNm()).isEqualTo("높음");
    }

    @Test
    @DisplayName("공통코드 상세를 성공적으로 생성한다")
    void createDetail_success() {
        // given
        CommonCodeDetailCreateRequest req = new CommonCodeDetailCreateRequest(
                "LOW", "낮음", 3);
        given(commonCodeRepository.findById(1L)).willReturn(Optional.of(commonCode));
        given(commonCodeDetailRepository.existsByCommonCode_GroupIdAndCodeVal(1L, "LOW")).willReturn(false);
        given(commonCodeDetailRepository.save(any(CommonCodeDetail.class))).willAnswer(invocation -> {
            CommonCodeDetail saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "detailId", 2L);
            return saved;
        });

        // when
        CommonCodeDetailResponse result = commonCodeService.createDetail(1L, req, 1L);

        // then
        assertThat(result.getCodeVal()).isEqualTo("LOW");
        assertThat(result.getCodeNm()).isEqualTo("낮음");
        assertThat(result.getSortOrder()).isEqualTo(3);
        verify(commonCodeDetailRepository).save(any(CommonCodeDetail.class));
    }

    @Test
    @DisplayName("중복 코드값으로 상세 생성 시 예외가 발생한다")
    void createDetail_duplicateCodeVal_throwsException() {
        // given
        CommonCodeDetailCreateRequest req = new CommonCodeDetailCreateRequest(
                "HIGH", "높음", 1);
        given(commonCodeRepository.findById(1L)).willReturn(Optional.of(commonCode));
        given(commonCodeDetailRepository.existsByCommonCode_GroupIdAndCodeVal(1L, "HIGH")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> commonCodeService.createDetail(1L, req, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_VALUE);
    }

    @Test
    @DisplayName("그룹코드로 활성 상세 목록을 조회한다")
    void getActiveDetailsByGroupCd_returnsActiveDetails() {
        // given
        given(commonCodeRepository.findByGroupCd("PRIORITY")).willReturn(Optional.of(commonCode));
        given(commonCodeDetailRepository.findByCommonCode_GroupIdAndIsActiveOrderBySortOrder(1L, "Y"))
                .willReturn(List.of(commonCodeDetail));

        // when
        List<CommonCodeDetailResponse> result = commonCodeService.getActiveDetailsByGroupCd("PRIORITY");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCodeVal()).isEqualTo("HIGH");
        assertThat(result.get(0).getIsActive()).isEqualTo("Y");
    }
}
