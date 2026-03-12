package com.itsm.api.controller.inspection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.api.dto.inspection.*;
import com.itsm.api.exception.GlobalExceptionHandler;
import com.itsm.api.service.inspection.InspectionService;
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
class InspectionControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private InspectionService inspectionService;

    @InjectMocks
    private InspectionController inspectionController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(inspectionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/inspections - 점검 목록 조회 시 200을 반환한다")
    void search_returns200() throws Exception {
        InspectionResponse response = InspectionResponse.builder()
                .inspectionId(1L).title("서버 정기점검").statusCd("SCHEDULED")
                .companyId(1L).companyNm("테스트회사")
                .scheduledAt(LocalDate.of(2026, 3, 20))
                .createdAt(LocalDateTime.now()).build();
        Page<InspectionResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        given(inspectionService.search(any(), any(), any(), any(), any())).willReturn(page);

        mockMvc.perform(get("/api/v1/inspections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].inspectionId").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/inspections/{id} - 점검 상세 조회 시 200을 반환한다")
    void getDetail_returns200() throws Exception {
        InspectionResponse response = InspectionResponse.builder()
                .inspectionId(1L).title("서버 정기점검").statusCd("SCHEDULED")
                .inspectionTypeCd("MONTHLY").companyId(1L).companyNm("테스트회사")
                .scheduledAt(LocalDate.of(2026, 3, 20))
                .itemCount(5).completedItemCount(2)
                .createdAt(LocalDateTime.now()).build();
        given(inspectionService.getDetail(1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/inspections/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inspectionId").value(1))
                .andExpect(jsonPath("$.data.title").value("서버 정기점검"))
                .andExpect(jsonPath("$.data.itemCount").value(5));
    }

    @Test
    @DisplayName("POST /api/v1/inspections - 점검 생성 시 200을 반환한다")
    void create_returns200() throws Exception {
        InspectionCreateRequest req = new InspectionCreateRequest(
                "서버 정기점검", "MONTHLY", LocalDate.of(2026, 3, 20), 1L, null, "월별 정기점검");
        InspectionResponse response = InspectionResponse.builder()
                .inspectionId(1L).title("서버 정기점검").statusCd("SCHEDULED")
                .createdAt(LocalDateTime.now()).build();
        given(inspectionService.create(any(InspectionCreateRequest.class), eq(1L))).willReturn(response);

        mockMvc.perform(post("/api/v1/inspections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inspectionId").value(1));
    }

    @Test
    @DisplayName("PATCH /api/v1/inspections/{id} - 점검 수정 시 200을 반환한다")
    void update_returns200() throws Exception {
        InspectionUpdateRequest req = new InspectionUpdateRequest(
                "네트워크 점검", "WEEKLY", LocalDate.of(2026, 4, 1), null, "주별 점검");
        InspectionResponse response = InspectionResponse.builder()
                .inspectionId(1L).title("네트워크 점검").statusCd("SCHEDULED")
                .createdAt(LocalDateTime.now()).build();
        given(inspectionService.update(eq(1L), any(InspectionUpdateRequest.class), eq(1L))).willReturn(response);

        mockMvc.perform(patch("/api/v1/inspections/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("네트워크 점검"));
    }

    @Test
    @DisplayName("PATCH /api/v1/inspections/{id}/status - 상태 변경 시 200을 반환한다")
    void changeStatus_returns200() throws Exception {
        mockMvc.perform(patch("/api/v1/inspections/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "IN_PROGRESS")))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(inspectionService).changeStatus(1L, "IN_PROGRESS", 1L);
    }

    @Test
    @DisplayName("POST /api/v1/inspections/{id}/items - 점검항목 추가 시 200을 반환한다")
    void addItem_returns200() throws Exception {
        InspectionItemRequest req = new InspectionItemRequest("CPU 사용률 점검", 1, "Y");
        InspectionItemResponse response = InspectionItemResponse.builder()
                .itemId(1L).itemNm("CPU 사용률 점검").sortOrder(1).isRequired("Y").build();
        given(inspectionService.addItem(eq(1L), any(InspectionItemRequest.class), eq(1L))).willReturn(response);

        mockMvc.perform(post("/api/v1/inspections/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.itemNm").value("CPU 사용률 점검"));
    }

    @Test
    @DisplayName("GET /api/v1/inspections/{id}/items - 점검항목 목록 조회 시 200을 반환한다")
    void getItems_returns200() throws Exception {
        InspectionItemResponse item = InspectionItemResponse.builder()
                .itemId(1L).itemNm("CPU 점검").sortOrder(1).isRequired("Y").build();
        given(inspectionService.getItems(1L)).willReturn(List.of(item));

        mockMvc.perform(get("/api/v1/inspections/1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].itemNm").value("CPU 점검"));
    }

    @Test
    @DisplayName("DELETE /api/v1/inspections/{id}/items/{itemId} - 점검항목 삭제 시 200을 반환한다")
    void deleteItem_returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/inspections/1/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(inspectionService).deleteItem(1L, 1L);
    }

    @Test
    @DisplayName("POST /api/v1/inspections/{id}/results - 점검결과 입력 시 200을 반환한다")
    void addResult_returns200() throws Exception {
        InspectionResultRequest req = new InspectionResultRequest(1L, "정상", "Y", "이상 없음");
        InspectionResultResponse response = InspectionResultResponse.builder()
                .resultId(1L).itemId(1L).itemNm("CPU 점검").resultValue("정상").isNormal("Y").build();
        given(inspectionService.addResult(eq(1L), any(InspectionResultRequest.class), eq(1L))).willReturn(response);

        mockMvc.perform(post("/api/v1/inspections/1/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resultValue").value("정상"));
    }

    @Test
    @DisplayName("GET /api/v1/inspections/{id}/results - 점검결과 목록 조회 시 200을 반환한다")
    void getResults_returns200() throws Exception {
        InspectionResultResponse result = InspectionResultResponse.builder()
                .resultId(1L).itemId(1L).itemNm("CPU 점검").resultValue("정상").isNormal("Y").build();
        given(inspectionService.getResults(1L)).willReturn(List.of(result));

        mockMvc.perform(get("/api/v1/inspections/1/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].resultValue").value("정상"));
    }

    @Test
    @DisplayName("GET /api/v1/inspections/{id}/history - 변경이력 조회 시 200을 반환한다")
    void getHistory_returns200() throws Exception {
        InspectionHistoryResponse history = InspectionHistoryResponse.builder()
                .historyId(1L).changedField("statusCd")
                .beforeValue("SCHEDULED").afterValue("IN_PROGRESS")
                .createdBy(1L).createdAt(LocalDateTime.now()).build();
        given(inspectionService.getHistory(1L)).willReturn(List.of(history));

        mockMvc.perform(get("/api/v1/inspections/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].changedField").value("statusCd"));
    }

    private UsernamePasswordAuthenticationToken createAuthentication(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
