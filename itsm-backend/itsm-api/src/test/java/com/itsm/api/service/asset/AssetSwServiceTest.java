package com.itsm.api.service.asset;

import com.itsm.api.dto.asset.*;
import com.itsm.core.domain.asset.AssetSw;
import com.itsm.core.domain.asset.AssetSwHistory;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.user.User;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.asset.AssetSwHistoryRepository;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AssetSwServiceTest {

    @Mock
    private AssetSwRepository assetSwRepository;

    @Mock
    private AssetSwHistoryRepository assetSwHistoryRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AssetSwService assetSwService;

    private Company company;
    private User manager;
    private AssetSw assetSw;

    @BeforeEach
    void setUp() {
        company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        ReflectionTestUtils.setField(company, "companyId", 1L);

        manager = User.builder().loginId("mgr01").password("pw").userNm("매니저").build();
        ReflectionTestUtils.setField(manager, "userId", 10L);

        assetSw = AssetSw.builder()
                .swNm("Oracle DB")
                .swTypeCd("DATABASE")
                .version("19c")
                .licenseKey("ORACLE-LIC-001")
                .licenseCnt(10)
                .company(company)
                .manager(manager)
                .description("메인 DB")
                .build();
        ReflectionTestUtils.setField(assetSw, "assetSwId", 1L);
    }

    @Test
    @DisplayName("SW 자산 목록을 검색 조건으로 조회한다")
    void search_returnsList() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<AssetSw> page = new PageImpl<>(List.of(assetSw), pageable, 1);
        given(assetSwRepository.search(null, null, null, null, pageable)).willReturn(page);

        // when
        Page<AssetSwResponse> result = assetSwService.search(null, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSwNm()).isEqualTo("Oracle DB");
    }

    @Test
    @DisplayName("SW 자산 상세를 조회한다")
    void getDetail_returnsResponse() {
        // given
        given(assetSwRepository.findById(1L)).willReturn(Optional.of(assetSw));

        // when
        AssetSwResponse result = assetSwService.getDetail(1L);

        // then
        assertThat(result.getAssetSwId()).isEqualTo(1L);
        assertThat(result.getSwNm()).isEqualTo("Oracle DB");
    }

    @Test
    @DisplayName("존재하지 않는 SW 자산 상세 조회 시 예외가 발생한다")
    void getDetail_notFound_throwsException() {
        // given
        given(assetSwRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> assetSwService.getDetail(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("SW 자산을 성공적으로 생성한다")
    void create_success() {
        // given
        AssetSwCreateRequest req = new AssetSwCreateRequest(
                "MySQL", "DATABASE", "8.0", "MYSQL-LIC-001", 5,
                LocalDate.of(2025, 1, 1), LocalDate.of(2027, 1, 1),
                1L, 10L, "새 DB");

        given(companyRepository.findById(1L)).willReturn(Optional.of(company));
        given(userRepository.findById(10L)).willReturn(Optional.of(manager));
        given(assetSwRepository.save(any(AssetSw.class))).willAnswer(invocation -> {
            AssetSw saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "assetSwId", 2L);
            return saved;
        });

        // when
        AssetSwResponse result = assetSwService.create(req, 1L);

        // then
        assertThat(result.getSwNm()).isEqualTo("MySQL");
        verify(assetSwRepository).save(any(AssetSw.class));
    }

    @Test
    @DisplayName("SW 자산을 수정하면 변경 이력이 저장된다")
    void update_savesHistory() {
        // given
        AssetSwUpdateRequest req = new AssetSwUpdateRequest(
                "Oracle DB-변경", "DATABASE", "21c", "ORACLE-LIC-002", 20,
                null, null, 10L, "변경됨");

        given(assetSwRepository.findById(1L)).willReturn(Optional.of(assetSw));
        given(userRepository.findById(10L)).willReturn(Optional.of(manager));

        // when
        AssetSwResponse result = assetSwService.update(1L, req, 1L);

        // then
        assertThat(result.getSwNm()).isEqualTo("Oracle DB-변경");
        assertThat(result.getVersion()).isEqualTo("21c");
        verify(assetSwHistoryRepository).saveAll(any());
    }

    @Test
    @DisplayName("SW 자산 상태를 변경한다")
    void changeStatus_success() {
        // given
        given(assetSwRepository.findById(1L)).willReturn(Optional.of(assetSw));

        // when
        assetSwService.changeStatus(1L, "INACTIVE", 1L);

        // then
        assertThat(assetSw.getStatus()).isEqualTo("INACTIVE");
        verify(assetSwHistoryRepository).save(any(AssetSwHistory.class));
    }

    @Test
    @DisplayName("SW 자산 변경 이력을 조회한다")
    void getHistory_returnsList() {
        // given
        AssetSwHistory history = AssetSwHistory.builder()
                .assetSwId(1L)
                .changedField("swNm")
                .beforeValue("Oracle DB")
                .afterValue("MySQL")
                .createdBy(1L)
                .build();
        ReflectionTestUtils.setField(history, "historyId", 1L);
        given(assetSwHistoryRepository.findByAssetSwIdOrderByCreatedAtDesc(1L)).willReturn(List.of(history));

        // when
        List<AssetHistoryResponse> result = assetSwService.getHistory(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChangedField()).isEqualTo("swNm");
    }
}
