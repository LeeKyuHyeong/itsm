package com.itsm.api.controller.asset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.api.dto.asset.*;
import com.itsm.api.exception.GlobalExceptionHandler;
import com.itsm.api.service.asset.AssetOaService;
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
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AssetOaControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AssetOaService assetOaService;

    @InjectMocks
    private AssetOaController assetOaController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(assetOaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/assets/oa - OA 자산 목록 조회 시 200을 반환한다")
    void search_returns200() throws Exception {
        // given
        AssetOaResponse response = AssetOaResponse.builder()
                .assetOaId(1L).assetNm("프린터#1").assetTypeCd("PRINTER")
                .companyId(1L).companyNm("테스트회사").status("ACTIVE")
                .createdAt(LocalDateTime.now()).build();
        Page<AssetOaResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        given(assetOaService.search(any(), any(), any(), any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/assets/oa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].assetOaId").value(1))
                .andExpect(jsonPath("$.data.content[0].assetNm").value("프린터#1"));
    }

    @Test
    @DisplayName("GET /api/v1/assets/oa/{id} - OA 자산 상세 조회 시 200을 반환한다")
    void getDetail_returns200() throws Exception {
        // given
        AssetOaResponse response = AssetOaResponse.builder()
                .assetOaId(1L).assetNm("프린터#1").assetTypeCd("PRINTER")
                .manufacturer("HP").modelNm("LaserJet Pro")
                .companyId(1L).companyNm("테스트회사").status("ACTIVE")
                .createdAt(LocalDateTime.now()).build();
        given(assetOaService.getDetail(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/assets/oa/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assetOaId").value(1))
                .andExpect(jsonPath("$.data.manufacturer").value("HP"));
    }

    @Test
    @DisplayName("POST /api/v1/assets/oa - OA 자산 생성 시 200을 반환한다")
    void create_returns200() throws Exception {
        // given
        AssetOaCreateRequest req = new AssetOaCreateRequest(
                "프린터#2", "PRINTER",
                "Canon", "PIXMA", "OA-SN-002",
                "192.168.10.60", null, "2층 사무실",
                LocalDate.of(2025, 1, 1), LocalDate.of(2028, 1, 1),
                1L, null, "새 프린터");
        AssetOaResponse response = AssetOaResponse.builder()
                .assetOaId(2L).assetNm("프린터#2").assetTypeCd("PRINTER")
                .companyId(1L).status("ACTIVE").createdAt(LocalDateTime.now()).build();
        given(assetOaService.create(any(AssetOaCreateRequest.class), eq(1L))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/assets/oa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assetOaId").value(2));
    }

    @Test
    @DisplayName("PATCH /api/v1/assets/oa/{id} - OA 자산 수정 시 200을 반환한다")
    void update_returns200() throws Exception {
        // given
        AssetOaUpdateRequest req = new AssetOaUpdateRequest(
                "프린터#1-변경", "PRINTER",
                "HP", "LaserJet Pro", "OA-SN-001",
                "192.168.10.100", null, "3층 사무실", null, null, null, "변경됨");
        AssetOaResponse response = AssetOaResponse.builder()
                .assetOaId(1L).assetNm("프린터#1-변경").status("ACTIVE")
                .createdAt(LocalDateTime.now()).build();
        given(assetOaService.update(eq(1L), any(AssetOaUpdateRequest.class), eq(1L))).willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v1/assets/oa/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assetNm").value("프린터#1-변경"));
    }

    @Test
    @DisplayName("PATCH /api/v1/assets/oa/{id}/status - OA 자산 상태 변경 시 200을 반환한다")
    void changeStatus_returns200() throws Exception {
        // when & then
        mockMvc.perform(patch("/api/v1/assets/oa/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "DISPOSED")))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(assetOaService).changeStatus(1L, "DISPOSED", 1L);
    }

    @Test
    @DisplayName("GET /api/v1/assets/oa/{id}/history - OA 자산 변경이력 조회 시 200을 반환한다")
    void getHistory_returns200() throws Exception {
        // given
        AssetHistoryResponse history = AssetHistoryResponse.builder()
                .historyId(1L).changedField("assetNm")
                .beforeValue("프린터#1").afterValue("프린터#2")
                .createdBy(1L).createdAt(LocalDateTime.now()).build();
        given(assetOaService.getHistory(1L)).willReturn(List.of(history));

        // when & then
        mockMvc.perform(get("/api/v1/assets/oa/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].changedField").value("assetNm"));
    }

    private UsernamePasswordAuthenticationToken createAuthentication(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
