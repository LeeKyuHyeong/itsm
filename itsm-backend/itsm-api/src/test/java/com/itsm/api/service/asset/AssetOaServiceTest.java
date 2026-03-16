package com.itsm.api.service.asset;

import com.itsm.api.dto.asset.*;
import com.itsm.core.domain.asset.AssetOa;
import com.itsm.core.domain.asset.AssetOaHistory;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.user.User;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.asset.AssetOaHistoryRepository;
import com.itsm.core.repository.asset.AssetOaRepository;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AssetOaServiceTest {

    @Mock
    private AssetOaRepository assetOaRepository;

    @Mock
    private AssetOaHistoryRepository assetOaHistoryRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AssetOaService assetOaService;

    private Company company;
    private User manager;
    private AssetOa assetOa;

    @BeforeEach
    void setUp() {
        company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        ReflectionTestUtils.setField(company, "companyId", 1L);

        manager = User.builder().loginId("mgr01").password("pw").userNm("매니저").build();
        ReflectionTestUtils.setField(manager, "userId", 10L);

        assetOa = AssetOa.builder()
                .assetNm("프린터#1")
                .assetTypeCd("PRINTER")
                .manufacturer("HP")
                .modelNm("LaserJet Pro")
                .serialNo("OA-SN-001")
                .ipAddress("192.168.10.50")
                .company(company)
                .manager(manager)
                .description("사무실 프린터")
                .build();
        ReflectionTestUtils.setField(assetOa, "assetOaId", 1L);
    }

    @Test
    @DisplayName("OA 자산 목록을 검색 조건으로 조회한다")
    void search_returnsList() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<AssetOa> page = new PageImpl<>(List.of(assetOa), pageable, 1);
        given(assetOaRepository.search(null, null, null, null, pageable)).willReturn(page);

        // when
        Page<AssetOaResponse> result = assetOaService.search(null, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAssetNm()).isEqualTo("프린터#1");
        assertThat(result.getContent().get(0).getCompanyNm()).isEqualTo("테스트회사");
    }

    @Test
    @DisplayName("OA 자산 상세를 조회한다")
    void getDetail_returnsResponse() {
        // given
        given(assetOaRepository.findById(1L)).willReturn(Optional.of(assetOa));

        // when
        AssetOaResponse result = assetOaService.getDetail(1L);

        // then
        assertThat(result.getAssetOaId()).isEqualTo(1L);
        assertThat(result.getAssetNm()).isEqualTo("프린터#1");
        assertThat(result.getManagerNm()).isEqualTo("매니저");
    }

    @Test
    @DisplayName("존재하지 않는 OA 자산 상세 조회 시 예외가 발생한다")
    void getDetail_notFound_throwsException() {
        // given
        given(assetOaRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> assetOaService.getDetail(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("OA 자산을 성공적으로 생성한다")
    void create_success() {
        // given
        AssetOaCreateRequest req = new AssetOaCreateRequest(
                "프린터#2", "PRINTER",
                "Canon", "PIXMA", "OA-SN-002",
                "192.168.10.60", "BB:CC:DD:EE:FF:02", "2층 사무실",
                LocalDate.of(2025, 1, 1), LocalDate.of(2028, 1, 1),
                1L, 10L, "새 프린터");

        given(companyRepository.findById(1L)).willReturn(Optional.of(company));
        given(userRepository.findById(10L)).willReturn(Optional.of(manager));
        given(assetOaRepository.save(any(AssetOa.class))).willAnswer(invocation -> {
            AssetOa saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "assetOaId", 2L);
            return saved;
        });

        // when
        AssetOaResponse result = assetOaService.create(req, 1L);

        // then
        assertThat(result.getAssetNm()).isEqualTo("프린터#2");
        assertThat(result.getAssetTypeCd()).isEqualTo("PRINTER");
        verify(assetOaRepository).save(any(AssetOa.class));
    }

    @Test
    @DisplayName("OA 자산을 수정하면 변경 이력이 저장된다")
    void update_savesHistory() {
        // given
        AssetOaUpdateRequest req = new AssetOaUpdateRequest(
                "프린터#1-변경", "PRINTER",
                "HP", "LaserJet Pro", "OA-SN-001",
                "192.168.10.100", "AA:BB:CC:DD:EE:01", "3층 사무실",
                null, null, 10L, "변경됨");

        given(assetOaRepository.findById(1L)).willReturn(Optional.of(assetOa));
        given(userRepository.findById(10L)).willReturn(Optional.of(manager));

        // when
        AssetOaResponse result = assetOaService.update(1L, req, 1L);

        // then
        assertThat(result.getAssetNm()).isEqualTo("프린터#1-변경");
        assertThat(result.getIpAddress()).isEqualTo("192.168.10.100");
        verify(assetOaHistoryRepository).saveAll(any());
    }

    @Test
    @DisplayName("OA 자산 상태를 변경한다")
    void changeStatus_success() {
        // given
        given(assetOaRepository.findById(1L)).willReturn(Optional.of(assetOa));

        // when
        assetOaService.changeStatus(1L, "DISPOSED", 1L);

        // then
        assertThat(assetOa.getStatus()).isEqualTo("DISPOSED");
        verify(assetOaHistoryRepository).save(any(AssetOaHistory.class));
    }

    @Test
    @DisplayName("OA 자산 변경 이력을 조회한다")
    void getHistory_returnsList() {
        // given
        AssetOaHistory history = AssetOaHistory.builder()
                .assetOaId(1L)
                .changedField("assetNm")
                .beforeValue("프린터#1")
                .afterValue("프린터#2")
                .createdBy(1L)
                .build();
        ReflectionTestUtils.setField(history, "historyId", 1L);
        given(assetOaHistoryRepository.findByAssetOaIdOrderByCreatedAtDesc(1L)).willReturn(List.of(history));

        // when
        List<AssetHistoryResponse> result = assetOaService.getHistory(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChangedField()).isEqualTo("assetNm");
    }
}
