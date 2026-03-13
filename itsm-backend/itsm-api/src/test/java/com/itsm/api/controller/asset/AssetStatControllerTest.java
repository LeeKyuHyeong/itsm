package com.itsm.api.controller.asset;

import com.itsm.api.dto.asset.AssetStatResponse;
import com.itsm.api.exception.GlobalExceptionHandler;
import com.itsm.api.service.asset.AssetStatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AssetStatControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AssetStatService assetStatService;

    @InjectMocks
    private AssetStatController assetStatController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(assetStatController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/assets/stats - 자산 통계 조회 시 200을 반환한다")
    void getStats_returns200() throws Exception {
        // given
        Map<String, Long> categoryCounts = new LinkedHashMap<>();
        categoryCounts.put("INFRA_HW", 5L);
        categoryCounts.put("INFRA_SW", 4L);
        categoryCounts.put("OA", 3L);

        Map<String, Map<String, Long>> subCategoryCounts = new LinkedHashMap<>();
        Map<String, Long> hwSub = new LinkedHashMap<>();
        hwSub.put("SERVER_RACK", 3L);
        hwSub.put("NETWORK_SWITCH", 2L);
        subCategoryCounts.put("INFRA_HW", hwSub);

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        statusCounts.put("ACTIVE", 9L);
        statusCounts.put("DISPOSED", 3L);

        AssetStatResponse response = AssetStatResponse.builder()
                .categoryCounts(categoryCounts)
                .subCategoryCounts(subCategoryCounts)
                .statusCounts(statusCounts)
                .build();
        given(assetStatService.getStats()).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/assets/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categoryCounts.INFRA_HW").value(5))
                .andExpect(jsonPath("$.data.categoryCounts.INFRA_SW").value(4))
                .andExpect(jsonPath("$.data.categoryCounts.OA").value(3))
                .andExpect(jsonPath("$.data.subCategoryCounts.INFRA_HW.SERVER_RACK").value(3))
                .andExpect(jsonPath("$.data.statusCounts.ACTIVE").value(9));
    }
}
