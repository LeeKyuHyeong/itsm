package com.itsm.api.controller.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.api.dto.common.*;
import com.itsm.api.exception.GlobalExceptionHandler;
import com.itsm.api.service.common.CommonCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class CommonCodeControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CommonCodeService commonCodeService;

    @InjectMocks
    private CommonCodeController commonCodeController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(commonCodeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/admin/common-codes - 공통코드 그룹 목록 조회 시 200을 반환한다")
    void getGroups_returns200() throws Exception {
        // given
        CommonCodeGroupResponse groupResponse = CommonCodeGroupResponse.builder()
                .groupId(1L)
                .groupNm("우선순위")
                .groupNmEn("Priority")
                .groupCd("PRIORITY")
                .description("우선순위 코드")
                .isActive("Y")
                .createdAt(LocalDateTime.now())
                .detailCount(3)
                .build();

        given(commonCodeService.getGroups()).willReturn(List.of(groupResponse));

        // when & then
        mockMvc.perform(get("/api/v1/admin/common-codes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].groupId").value(1))
                .andExpect(jsonPath("$.data[0].groupNm").value("우선순위"))
                .andExpect(jsonPath("$.data[0].groupNmEn").value("Priority"))
                .andExpect(jsonPath("$.data[0].groupCd").value("PRIORITY"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/common-codes - 공통코드 그룹 생성 시 200을 반환한다")
    void createGroup_returns200() throws Exception {
        // given
        CommonCodeGroupCreateRequest req = new CommonCodeGroupCreateRequest(
                "상태", "Status", "STATUS", "상태 코드");

        CommonCodeGroupResponse response = CommonCodeGroupResponse.builder()
                .groupId(2L)
                .groupNm("상태")
                .groupNmEn("Status")
                .groupCd("STATUS")
                .description("상태 코드")
                .isActive("Y")
                .createdAt(LocalDateTime.now())
                .detailCount(0)
                .build();

        given(commonCodeService.createGroup(any(CommonCodeGroupCreateRequest.class), eq(1L)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/admin/common-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.groupId").value(2))
                .andExpect(jsonPath("$.data.groupNm").value("상태"))
                .andExpect(jsonPath("$.data.groupCd").value("STATUS"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/common-codes/{groupId}/details - 공통코드 상세 목록 조회 시 200을 반환한다")
    void getDetails_returns200() throws Exception {
        // given
        CommonCodeDetailResponse detailResponse = CommonCodeDetailResponse.builder()
                .detailId(1L)
                .codeVal("HIGH")
                .codeNm("높음")
                .codeNmEn("High")
                .sortOrder(1)
                .isActive("Y")
                .createdAt(LocalDateTime.now())
                .build();

        given(commonCodeService.getDetails(1L)).willReturn(List.of(detailResponse));

        // when & then
        mockMvc.perform(get("/api/v1/admin/common-codes/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].detailId").value(1))
                .andExpect(jsonPath("$.data[0].codeVal").value("HIGH"))
                .andExpect(jsonPath("$.data[0].codeNm").value("높음"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/common-codes/{groupId}/details - 공통코드 상세 생성 시 200을 반환한다")
    void createDetail_returns200() throws Exception {
        // given
        CommonCodeDetailCreateRequest req = new CommonCodeDetailCreateRequest(
                "LOW", "낮음", "Low", 3);

        CommonCodeDetailResponse response = CommonCodeDetailResponse.builder()
                .detailId(2L)
                .codeVal("LOW")
                .codeNm("낮음")
                .codeNmEn("Low")
                .sortOrder(3)
                .isActive("Y")
                .createdAt(LocalDateTime.now())
                .build();

        given(commonCodeService.createDetail(eq(1L), any(CommonCodeDetailCreateRequest.class), eq(1L)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/admin/common-codes/1/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.detailId").value(2))
                .andExpect(jsonPath("$.data.codeVal").value("LOW"))
                .andExpect(jsonPath("$.data.codeNm").value("낮음"));
    }

    @Test
    @DisplayName("GET /api/v1/common-codes/{groupCd} - 공개 API로 활성 상세 목록 조회 시 200을 반환한다")
    void getActiveDetailsByGroupCd_returns200() throws Exception {
        // given
        CommonCodeDetailResponse detailResponse = CommonCodeDetailResponse.builder()
                .detailId(1L)
                .codeVal("HIGH")
                .codeNm("높음")
                .codeNmEn("High")
                .sortOrder(1)
                .isActive("Y")
                .createdAt(LocalDateTime.now())
                .build();

        given(commonCodeService.getActiveDetailsByGroupCd("PRIORITY"))
                .willReturn(List.of(detailResponse));

        // when & then
        mockMvc.perform(get("/api/v1/common-codes/PRIORITY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].codeVal").value("HIGH"))
                .andExpect(jsonPath("$.data[0].codeNm").value("높음"));
    }

    private UsernamePasswordAuthenticationToken createAuthentication(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
