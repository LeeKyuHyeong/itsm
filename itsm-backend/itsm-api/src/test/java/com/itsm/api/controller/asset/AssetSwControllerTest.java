package com.itsm.api.controller.asset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.api.dto.asset.*;
import com.itsm.api.exception.GlobalExceptionHandler;
import com.itsm.api.service.asset.AssetSwService;
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
class AssetSwControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AssetSwService assetSwService;

    @InjectMocks
    private AssetSwController assetSwController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(assetSwController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/assets/sw - SW 자산 목록 조회 시 200을 반환한다")
    void search_returns200() throws Exception {
        // given
        AssetSwResponse response = AssetSwResponse.builder()
                .assetSwId(1L).swNm("Oracle DB").swTypeCd("DATABASE")
                .companyId(1L).companyNm("테스트회사").status("ACTIVE")
                .createdAt(LocalDateTime.now()).build();
        Page<AssetSwResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        given(assetSwService.search(any(), any(), any(), any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/assets/sw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].swNm").value("Oracle DB"));
    }

    @Test
    @DisplayName("GET /api/v1/assets/sw/{id} - SW 자산 상세 조회 시 200을 반환한다")
    void getDetail_returns200() throws Exception {
        // given
        AssetSwResponse response = AssetSwResponse.builder()
                .assetSwId(1L).swNm("Oracle DB").swTypeCd("DATABASE")
                .version("19c").companyId(1L).status("ACTIVE")
                .createdAt(LocalDateTime.now()).build();
        given(assetSwService.getDetail(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/assets/sw/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.swNm").value("Oracle DB"))
                .andExpect(jsonPath("$.data.version").value("19c"));
    }

    @Test
    @DisplayName("POST /api/v1/assets/sw - SW 자산 생성 시 200을 반환한다")
    void create_returns200() throws Exception {
        // given
        AssetSwCreateRequest req = new AssetSwCreateRequest(
                "MySQL", "DATABASE", "8.0", "LIC-001", 5,
                LocalDate.of(2025, 1, 1), LocalDate.of(2027, 1, 1),
                1L, null, "새 DB");
        AssetSwResponse response = AssetSwResponse.builder()
                .assetSwId(2L).swNm("MySQL").swTypeCd("DATABASE")
                .companyId(1L).status("ACTIVE").createdAt(LocalDateTime.now()).build();
        given(assetSwService.create(any(AssetSwCreateRequest.class), eq(1L))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/assets/sw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assetSwId").value(2))
                .andExpect(jsonPath("$.data.swNm").value("MySQL"));
    }

    @Test
    @DisplayName("PATCH /api/v1/assets/sw/{id} - SW 자산 수정 시 200을 반환한다")
    void update_returns200() throws Exception {
        // given
        AssetSwUpdateRequest req = new AssetSwUpdateRequest(
                "Oracle DB-변경", "DATABASE", "21c", "LIC-002", 20,
                null, null, null, "변경됨");
        AssetSwResponse response = AssetSwResponse.builder()
                .assetSwId(1L).swNm("Oracle DB-변경").version("21c")
                .status("ACTIVE").createdAt(LocalDateTime.now()).build();
        given(assetSwService.update(eq(1L), any(AssetSwUpdateRequest.class), eq(1L))).willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v1/assets/sw/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.swNm").value("Oracle DB-변경"));
    }

    @Test
    @DisplayName("PATCH /api/v1/assets/sw/{id}/status - SW 자산 상태 변경 시 200을 반환한다")
    void changeStatus_returns200() throws Exception {
        // when & then
        mockMvc.perform(patch("/api/v1/assets/sw/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "INACTIVE")))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(assetSwService).changeStatus(1L, "INACTIVE", 1L);
    }

    @Test
    @DisplayName("GET /api/v1/assets/sw/{id}/history - SW 자산 변경이력 조회 시 200을 반환한다")
    void getHistory_returns200() throws Exception {
        // given
        AssetHistoryResponse history = AssetHistoryResponse.builder()
                .historyId(1L).changedField("swNm")
                .beforeValue("Oracle DB").afterValue("MySQL")
                .createdBy(1L).createdAt(LocalDateTime.now()).build();
        given(assetSwService.getHistory(1L)).willReturn(List.of(history));

        // when & then
        mockMvc.perform(get("/api/v1/assets/sw/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].changedField").value("swNm"));
    }

    private UsernamePasswordAuthenticationToken createAuthentication(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
