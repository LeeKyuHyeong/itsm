package com.itsm.api.service.asset;

import com.itsm.api.dto.asset.AssetStatResponse;
import com.itsm.core.repository.asset.AssetHwRepository;
import com.itsm.core.repository.asset.AssetSwRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AssetStatServiceTest {

    @Mock
    private AssetHwRepository assetHwRepository;

    @Mock
    private AssetSwRepository assetSwRepository;

    @InjectMocks
    private AssetStatService assetStatService;

    @Test
    @DisplayName("자산 통계를 조회한다 - 카테고리별, 서브카테고리별, 상태별 카운트")
    void getStats_returnsCombinedStats() {
        // given
        given(assetHwRepository.countByCategory()).willReturn(Arrays.asList(
                new Object[]{"INFRA_HW", 5L},
                new Object[]{"OA", 3L}
        ));
        List<Object[]> swCatList = new ArrayList<>();
        swCatList.add(new Object[]{"INFRA_SW", 4L});
        given(assetSwRepository.countByCategory()).willReturn(swCatList);

        given(assetHwRepository.countBySubCategory("INFRA_HW")).willReturn(Arrays.asList(
                new Object[]{"SERVER_RACK", 3L},
                new Object[]{"NETWORK_SWITCH", 2L}
        ));
        given(assetHwRepository.countBySubCategory("OA")).willReturn(Arrays.asList(
                new Object[]{"OA_DESKTOP", 2L},
                new Object[]{"OA_LAPTOP", 1L}
        ));
        given(assetSwRepository.countBySubCategory("INFRA_SW")).willReturn(Arrays.asList(
                new Object[]{"SW_DB", 2L},
                new Object[]{"SW_OS", 2L}
        ));

        given(assetHwRepository.countByStatus()).willReturn(Arrays.asList(
                new Object[]{"ACTIVE", 6L},
                new Object[]{"DISPOSED", 2L}
        ));
        given(assetSwRepository.countByStatus()).willReturn(Arrays.asList(
                new Object[]{"ACTIVE", 3L},
                new Object[]{"INACTIVE", 1L}
        ));

        // when
        AssetStatResponse result = assetStatService.getStats();

        // then
        assertThat(result.getCategoryCounts()).containsEntry("INFRA_HW", 5L);
        assertThat(result.getCategoryCounts()).containsEntry("INFRA_SW", 4L);
        assertThat(result.getCategoryCounts()).containsEntry("OA", 3L);

        assertThat(result.getSubCategoryCounts()).containsKey("INFRA_HW");
        assertThat(result.getSubCategoryCounts().get("INFRA_HW")).containsEntry("SERVER_RACK", 3L);
        assertThat(result.getSubCategoryCounts()).containsKey("INFRA_SW");
        assertThat(result.getSubCategoryCounts().get("INFRA_SW")).containsEntry("SW_DB", 2L);
        assertThat(result.getSubCategoryCounts()).containsKey("OA");
        assertThat(result.getSubCategoryCounts().get("OA")).containsEntry("OA_DESKTOP", 2L);

        assertThat(result.getStatusCounts()).containsEntry("ACTIVE", 9L);
        assertThat(result.getStatusCounts()).containsEntry("DISPOSED", 2L);
        assertThat(result.getStatusCounts()).containsEntry("INACTIVE", 1L);
    }
}
