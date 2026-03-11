package com.itsm.api.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.api.dto.user.*;
import com.itsm.api.exception.GlobalExceptionHandler;
import com.itsm.api.service.user.UserService;
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
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/users - 사용자 목록 조회 시 200을 반환한다")
    void getUsers_returns200() throws Exception {
        // given
        UserListResponse userListResponse = UserListResponse.builder()
                .userId(1L)
                .loginId("testuser")
                .userNm("테스트사용자")
                .employeeNo("EMP001")
                .email("test@test.com")
                .status("ACTIVE")
                .deptName("IT부서")
                .companyName("테스트회사")
                .roles(List.of("ADMIN"))
                .build();

        Page<UserListResponse> page = new PageImpl<>(
                List.of(userListResponse), PageRequest.of(0, 10), 1);
        given(userService.getUsers(isNull(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].userId").value(1))
                .andExpect(jsonPath("$.data.content[0].loginId").value("testuser"))
                .andExpect(jsonPath("$.data.content[0].userNm").value("테스트사용자"));
    }

    @Test
    @DisplayName("POST /api/v1/users - 사용자 생성 시 200을 반환한다")
    void createUser_returns200() throws Exception {
        // given
        UserCreateRequest req = new UserCreateRequest(
                "newuser", "Password1!", "신규사용자", "EMP002", 1L, "new@test.com", "010-0000-0000");

        UserDetailResponse response = UserDetailResponse.builder()
                .userId(2L)
                .loginId("newuser")
                .userNm("신규사용자")
                .employeeNo("EMP002")
                .deptId(1L)
                .deptName("IT부서")
                .email("new@test.com")
                .tel("010-0000-0000")
                .status("ACTIVE")
                .validFrom(LocalDateTime.now())
                .roles(Collections.emptyList())
                .build();

        given(userService.createUser(any(UserCreateRequest.class), eq(1L))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(2))
                .andExpect(jsonPath("$.data.loginId").value("newuser"))
                .andExpect(jsonPath("$.data.userNm").value("신규사용자"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - 사용자 상세 조회 시 200을 반환한다")
    void getUser_returns200() throws Exception {
        // given
        UserDetailResponse response = UserDetailResponse.builder()
                .userId(1L)
                .loginId("testuser")
                .userNm("테스트사용자")
                .employeeNo("EMP001")
                .deptId(1L)
                .deptName("IT부서")
                .companyId(1L)
                .companyName("테스트회사")
                .email("test@test.com")
                .tel("010-1234-5678")
                .status("ACTIVE")
                .validFrom(LocalDateTime.now())
                .roles(List.of(UserDetailResponse.RoleInfo.builder()
                        .roleId(1L)
                        .roleNm("관리자")
                        .roleCd("ADMIN")
                        .build()))
                .build();

        given(userService.getUser(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.loginId").value("testuser"))
                .andExpect(jsonPath("$.data.userNm").value("테스트사용자"))
                .andExpect(jsonPath("$.data.roles[0].roleCd").value("ADMIN"));
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{id}/status - 사용자 상태 변경 시 200을 반환한다")
    void changeStatus_returns200() throws Exception {
        // given
        UserStatusRequest req = new UserStatusRequest("INACTIVE");
        doNothing().when(userService).changeUserStatus(eq(1L), any(UserStatusRequest.class), eq(1L));

        // when & then
        mockMvc.perform(patch("/api/v1/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private UsernamePasswordAuthenticationToken createAuthentication(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
