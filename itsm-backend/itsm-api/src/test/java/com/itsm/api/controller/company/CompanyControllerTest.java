package com.itsm.api.controller.company;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.api.dto.company.*;
import com.itsm.api.exception.GlobalExceptionHandler;
import com.itsm.api.service.company.CompanyService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class CompanyControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CompanyService companyService;

    @InjectMocks
    private CompanyController companyController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(companyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/companies - 회사 목록 조회 시 200을 반환한다")
    void getCompanies_returns200() throws Exception {
        // given
        CompanyResponse companyResponse = CompanyResponse.builder()
                .companyId(1L)
                .companyNm("테스트회사")
                .bizNo("123-45-67890")
                .ceoNm("홍길동")
                .tel("02-1234-5678")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();

        Page<CompanyResponse> page = new PageImpl<>(
                List.of(companyResponse), PageRequest.of(0, 10), 1);
        given(companyService.getCompanies(isNull(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/companies")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].companyId").value(1))
                .andExpect(jsonPath("$.data.content[0].companyNm").value("테스트회사"));
    }

    @Test
    @DisplayName("POST /api/v1/companies - 회사 생성 시 200을 반환한다")
    void createCompany_returns200() throws Exception {
        // given
        CompanyCreateRequest req = new CompanyCreateRequest(
                "신규회사", "999-88-77777", "김철수", "02-9999-8888", null);

        CompanyResponse response = CompanyResponse.builder()
                .companyId(2L)
                .companyNm("신규회사")
                .bizNo("999-88-77777")
                .ceoNm("김철수")
                .tel("02-9999-8888")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();

        given(companyService.createCompany(any(CompanyCreateRequest.class), eq(1L))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.companyId").value(2))
                .andExpect(jsonPath("$.data.companyNm").value("신규회사"))
                .andExpect(jsonPath("$.data.bizNo").value("999-88-77777"));
    }

    @Test
    @DisplayName("GET /api/v1/companies/{id}/departments - 부서 목록 조회 시 200을 반환한다")
    void getDepartments_returns200() throws Exception {
        // given
        DepartmentResponse deptResponse = DepartmentResponse.builder()
                .deptId(1L)
                .deptNm("IT부서")
                .companyId(1L)
                .companyNm("테스트회사")
                .status("ACTIVE")
                .build();

        given(companyService.getDepartments(1L)).willReturn(List.of(deptResponse));

        // when & then
        mockMvc.perform(get("/api/v1/companies/1/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].deptId").value(1))
                .andExpect(jsonPath("$.data[0].deptNm").value("IT부서"))
                .andExpect(jsonPath("$.data[0].companyNm").value("테스트회사"));
    }

    private UsernamePasswordAuthenticationToken createAuthentication(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
