package com.itsm.api.controller.asset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.api.dto.asset.*;
import com.itsm.api.exception.GlobalExceptionHandler;
import com.itsm.api.service.asset.AssetHwService;
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
class AssetHwControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AssetHwService assetHwService;

    @InjectMocks
    private AssetHwController assetHwController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(assetHwController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/assets/hw - HW 자산 목록 조회 시 200을 반환한다")
    void search_returns200() throws Exception {
        // given
        AssetHwResponse response = AssetHwResponse.builder()
                .assetHwId(1L).assetNm("서버#1").assetTypeCd("SERVER")
                .companyId(1L).companyNm("테스트회사").status("ACTIVE")
                .createdAt(LocalDateTime.now()).build();
        Page<AssetHwResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        given(assetHwService.search(any(), any(), any(), any(), any(), any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/assets/hw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].assetHwId").value(1))
                .andExpect(jsonPath("$.data.content[0].assetNm").value("서버#1"));
    }

    @Test
    @DisplayName("GET /api/v1/assets/hw?assetCategory=INFRA_HW - 카테고리 필터 검색 시 200을 반환한다")
    void search_withCategoryFilter_returns200() throws Exception {
        // given
        AssetHwResponse response = AssetHwResponse.builder()
                .assetHwId(1L).assetNm("서버#1").assetTypeCd("SERVER")
                .assetCategory("INFRA_HW").assetSubCategory("SERVER_RACK")
                .companyId(1L).companyNm("테스트회사").status("ACTIVE")
                .createdAt(LocalDateTime.now()).build();
        Page<AssetHwResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        given(assetHwService.search(any(), any(), any(), any(), eq("INFRA_HW"), eq("SERVER_RACK"), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/assets/hw")
                        .param("assetCategory", "INFRA_HW")
                        .param("assetSubCategory", "SERVER_RACK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].assetCategory").value("INFRA_HW"))
                .andExpect(jsonPath("$.data.content[0].assetSubCategory").value("SERVER_RACK"));
    }

    @Test
    @DisplayName("GET /api/v1/assets/hw/{id} - HW 자산 상세 조회 시 200을 반환한다")
    void getDetail_returns200() throws Exception {
        // given
        AssetHwResponse response = AssetHwResponse.builder()
                .assetHwId(1L).assetNm("서버#1").assetTypeCd("SERVER")
                .manufacturer("Dell").modelNm("PowerEdge R740")
                .companyId(1L).companyNm("테스트회사").status("ACTIVE")
                .createdAt(LocalDateTime.now()).build();
        given(assetHwService.getDetail(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/assets/hw/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assetHwId").value(1))
                .andExpect(jsonPath("$.data.manufacturer").value("Dell"));
    }

    @Test
    @DisplayName("POST /api/v1/assets/hw - HW 자산 생성 시 200을 반환한다")
    void create_returns200() throws Exception {
        // given
        AssetHwCreateRequest req = new AssetHwCreateRequest(
                "서버#2", "SERVER", "INFRA_HW", "SERVER_RACK",
                "HP", "DL380", "SN-002",
                "10.0.0.1", null, "IDC 2층",
                LocalDate.of(2025, 1, 1), LocalDate.of(2028, 1, 1),
                1L, null, "새 서버");
        AssetHwResponse response = AssetHwResponse.builder()
                .assetHwId(2L).assetNm("서버#2").assetTypeCd("SERVER")
                .companyId(1L).status("ACTIVE").createdAt(LocalDateTime.now()).build();
        given(assetHwService.create(any(AssetHwCreateRequest.class), eq(1L))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/assets/hw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assetHwId").value(2));
    }

    @Test
    @DisplayName("PATCH /api/v1/assets/hw/{id} - HW 자산 수정 시 200을 반환한다")
    void update_returns200() throws Exception {
        // given
        AssetHwUpdateRequest req = new AssetHwUpdateRequest(
                "서버#1-변경", "SERVER", "INFRA_HW", "SERVER_RACK",
                "Dell", "R740", "SN-001",
                "192.168.1.200", null, "IDC 1층", null, null, null, "변경됨");
        AssetHwResponse response = AssetHwResponse.builder()
                .assetHwId(1L).assetNm("서버#1-변경").status("ACTIVE")
                .createdAt(LocalDateTime.now()).build();
        given(assetHwService.update(eq(1L), any(AssetHwUpdateRequest.class), eq(1L))).willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v1/assets/hw/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assetNm").value("서버#1-변경"));
    }

    @Test
    @DisplayName("PATCH /api/v1/assets/hw/{id}/status - HW 자산 상태 변경 시 200을 반환한다")
    void changeStatus_returns200() throws Exception {
        // when & then
        mockMvc.perform(patch("/api/v1/assets/hw/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "DISPOSED")))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(assetHwService).changeStatus(1L, "DISPOSED", 1L);
    }

    @Test
    @DisplayName("GET /api/v1/assets/hw/{id}/history - HW 자산 변경이력 조회 시 200을 반환한다")
    void getHistory_returns200() throws Exception {
        // given
        AssetHistoryResponse history = AssetHistoryResponse.builder()
                .historyId(1L).changedField("assetNm")
                .beforeValue("서버#1").afterValue("서버#2")
                .createdBy(1L).createdAt(LocalDateTime.now()).build();
        given(assetHwService.getHistory(1L)).willReturn(List.of(history));

        // when & then
        mockMvc.perform(get("/api/v1/assets/hw/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].changedField").value("assetNm"));
    }

    @Test
    @DisplayName("POST /api/v1/assets/hw/relations - 연관관계 등록 시 200을 반환한다")
    void addRelation_returns200() throws Exception {
        // given
        AssetRelationRequest req = new AssetRelationRequest(1L, 5L, LocalDate.now());
        AssetRelationResponse response = AssetRelationResponse.builder()
                .assetHwId(1L).assetSwId(5L).assetHwNm("서버#1").assetSwNm("Oracle DB")
                .createdAt(LocalDateTime.now()).build();
        given(assetHwService.addRelation(any(AssetRelationRequest.class), eq(1L))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/assets/hw/relations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assetHwId").value(1))
                .andExpect(jsonPath("$.data.assetSwId").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/assets/hw/{id}/relations - 연관 SW 목록 조회 시 200을 반환한다")
    void getRelations_returns200() throws Exception {
        // given
        AssetRelationResponse response = AssetRelationResponse.builder()
                .assetHwId(1L).assetSwId(5L).assetSwNm("Oracle DB")
                .createdAt(LocalDateTime.now()).build();
        given(assetHwService.getRelations(1L)).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/v1/assets/hw/1/relations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].assetSwNm").value("Oracle DB"));
    }

    private UsernamePasswordAuthenticationToken createAuthentication(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
