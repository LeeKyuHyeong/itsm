package com.itsm.api.service.asset;

import com.itsm.api.dto.asset.*;
import com.itsm.core.domain.asset.AssetHw;
import com.itsm.core.domain.asset.AssetHwHistory;
import com.itsm.core.domain.asset.AssetRelation;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.user.User;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.asset.AssetHwHistoryRepository;
import com.itsm.core.repository.asset.AssetHwRepository;
import com.itsm.core.repository.asset.AssetRelationRepository;
import com.itsm.core.repository.asset.AssetSwRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
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
class AssetHwServiceTest {

    @Mock
    private AssetHwRepository assetHwRepository;

    @Mock
    private AssetHwHistoryRepository assetHwHistoryRepository;

    @Mock
    private AssetRelationRepository assetRelationRepository;

    @Mock
    private AssetSwRepository assetSwRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AssetHwService assetHwService;

    private Company company;
    private User manager;
    private AssetHw assetHw;

    @BeforeEach
    void setUp() {
        company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        ReflectionTestUtils.setField(company, "companyId", 1L);

        manager = User.builder().loginId("mgr01").password("pw").userNm("매니저").build();
        ReflectionTestUtils.setField(manager, "userId", 10L);

        assetHw = AssetHw.builder()
                .assetNm("서버#1")
                .assetTypeCd("SERVER")
                .assetCategory("INFRA_HW")
                .assetSubCategory("SERVER_RACK")
                .manufacturer("Dell")
                .modelNm("PowerEdge R740")
                .serialNo("SN-001")
                .ipAddress("192.168.1.100")
                .company(company)
                .manager(manager)
                .description("메인 서버")
                .build();
        ReflectionTestUtils.setField(assetHw, "assetHwId", 1L);
    }

    @Test
    @DisplayName("HW 자산 목록을 검색 조건으로 조회한다")
    void search_returnsList() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<AssetHw> page = new PageImpl<>(List.of(assetHw), pageable, 1);
        given(assetHwRepository.search(null, null, null, null, null, null, pageable)).willReturn(page);

        // when
        Page<AssetHwResponse> result = assetHwService.search(null, null, null, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAssetNm()).isEqualTo("서버#1");
        assertThat(result.getContent().get(0).getCompanyNm()).isEqualTo("테스트회사");
    }

    @Test
    @DisplayName("HW 자산 상세를 조회한다")
    void getDetail_returnsResponse() {
        // given
        given(assetHwRepository.findById(1L)).willReturn(Optional.of(assetHw));

        // when
        AssetHwResponse result = assetHwService.getDetail(1L);

        // then
        assertThat(result.getAssetHwId()).isEqualTo(1L);
        assertThat(result.getAssetNm()).isEqualTo("서버#1");
        assertThat(result.getManagerNm()).isEqualTo("매니저");
    }

    @Test
    @DisplayName("존재하지 않는 HW 자산 상세 조회 시 예외가 발생한다")
    void getDetail_notFound_throwsException() {
        // given
        given(assetHwRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> assetHwService.getDetail(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("HW 자산을 성공적으로 생성한다")
    void create_success() {
        // given
        AssetHwCreateRequest req = new AssetHwCreateRequest(
                "서버#2", "SERVER", "INFRA_HW", "SERVER_RACK",
                "HP", "DL380", "SN-002",
                "10.0.0.1", "11:22:33:44:55:66", "IDC 2층",
                LocalDate.of(2025, 1, 1), LocalDate.of(2028, 1, 1),
                1L, 10L, "새 서버");

        given(companyRepository.findById(1L)).willReturn(Optional.of(company));
        given(userRepository.findById(10L)).willReturn(Optional.of(manager));
        given(assetHwRepository.save(any(AssetHw.class))).willAnswer(invocation -> {
            AssetHw saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "assetHwId", 2L);
            return saved;
        });

        // when
        AssetHwResponse result = assetHwService.create(req, 1L);

        // then
        assertThat(result.getAssetNm()).isEqualTo("서버#2");
        assertThat(result.getAssetTypeCd()).isEqualTo("SERVER");
        assertThat(result.getAssetCategory()).isEqualTo("INFRA_HW");
        assertThat(result.getAssetSubCategory()).isEqualTo("SERVER_RACK");
        verify(assetHwRepository).save(any(AssetHw.class));
    }

    @Test
    @DisplayName("HW 자산을 수정하면 변경 이력이 저장된다")
    void update_savesHistory() {
        // given
        AssetHwUpdateRequest req = new AssetHwUpdateRequest(
                "서버#1-변경", "SERVER", "INFRA_HW", "SERVER_RACK",
                "Dell", "PowerEdge R740", "SN-001",
                "192.168.1.200", "AA:BB:CC:DD:EE:FF", "IDC 1층",
                null, null, 10L, "변경됨");

        given(assetHwRepository.findById(1L)).willReturn(Optional.of(assetHw));
        given(userRepository.findById(10L)).willReturn(Optional.of(manager));

        // when
        AssetHwResponse result = assetHwService.update(1L, req, 1L);

        // then
        assertThat(result.getAssetNm()).isEqualTo("서버#1-변경");
        assertThat(result.getIpAddress()).isEqualTo("192.168.1.200");
        verify(assetHwHistoryRepository).saveAll(any());
    }

    @Test
    @DisplayName("HW 자산 상태를 변경한다")
    void changeStatus_success() {
        // given
        given(assetHwRepository.findById(1L)).willReturn(Optional.of(assetHw));

        // when
        assetHwService.changeStatus(1L, "DISPOSED", 1L);

        // then
        assertThat(assetHw.getStatus()).isEqualTo("DISPOSED");
        verify(assetHwHistoryRepository).save(any(AssetHwHistory.class));
    }

    @Test
    @DisplayName("HW 자산 변경 이력을 조회한다")
    void getHistory_returnsList() {
        // given
        AssetHwHistory history = AssetHwHistory.builder()
                .assetHwId(1L)
                .changedField("assetNm")
                .beforeValue("서버#1")
                .afterValue("서버#2")
                .createdBy(1L)
                .build();
        ReflectionTestUtils.setField(history, "historyId", 1L);
        given(assetHwHistoryRepository.findByAssetHwIdOrderByCreatedAtDesc(1L)).willReturn(List.of(history));

        // when
        List<AssetHistoryResponse> result = assetHwService.getHistory(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChangedField()).isEqualTo("assetNm");
    }

    @Test
    @DisplayName("HW-SW 연관관계를 등록한다")
    void addRelation_success() {
        // given
        AssetRelationRequest req = new AssetRelationRequest(1L, 5L, LocalDate.now());
        given(assetHwRepository.findById(1L)).willReturn(Optional.of(assetHw));
        given(assetSwRepository.findById(5L)).willReturn(Optional.of(
                com.itsm.core.domain.asset.AssetSw.builder()
                        .swNm("Oracle DB").swTypeCd("DATABASE").company(company).build()));
        given(assetRelationRepository.findById(any())).willReturn(Optional.empty());
        given(assetRelationRepository.save(any(AssetRelation.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        AssetRelationResponse result = assetHwService.addRelation(req, 1L);

        // then
        assertThat(result.getAssetHwId()).isEqualTo(1L);
        assertThat(result.getAssetSwId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("HW의 연관 SW 목록을 조회한다")
    void getRelations_returnsList() {
        // given
        AssetRelation relation = AssetRelation.builder()
                .assetHwId(1L).assetSwId(5L).installedAt(LocalDate.now()).createdBy(1L).build();
        given(assetRelationRepository.findByAssetHwIdAndRemovedAtIsNull(1L)).willReturn(List.of(relation));

        // when
        List<AssetRelationResponse> result = assetHwService.getRelations(1L);

        // then
        assertThat(result).hasSize(1);
    }
}
