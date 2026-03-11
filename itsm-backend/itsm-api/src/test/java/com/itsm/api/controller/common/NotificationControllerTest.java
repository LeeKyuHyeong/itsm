package com.itsm.api.controller.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.api.dto.common.NotificationResponse;
import com.itsm.api.exception.GlobalExceptionHandler;
import com.itsm.api.service.common.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class NotificationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/notifications - 알림 목록 조회 시 200을 반환한다")
    void getMyNotifications_returns200() throws Exception {
        // given
        NotificationResponse response = NotificationResponse.builder()
                .notiId(1L)
                .notiTypeCd("TICKET")
                .title("티켓이 할당되었습니다")
                .content("티켓 #100이 할당되었습니다.")
                .refType("TICKET")
                .refId(100L)
                .readAt(null)
                .createdAt(LocalDateTime.now())
                .build();

        given(notificationService.getMyNotifications(1L)).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/v1/notifications")
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].notiId").value(1))
                .andExpect(jsonPath("$.data[0].title").value("티켓이 할당되었습니다"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/unread-count - 읽지 않은 알림 수 조회 시 200을 반환한다")
    void getUnreadCount_returns200() throws Exception {
        // given
        given(notificationService.getUnreadCount(1L)).willReturn(5L);

        // when & then
        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    @DisplayName("PATCH /api/v1/notifications/{notiId}/read - 알림 읽음 처리 시 200을 반환한다")
    void markAsRead_returns200() throws Exception {
        // when & then
        mockMvc.perform(patch("/api/v1/notifications/1/read")
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationService).markAsRead(1L, 1L);
    }

    @Test
    @DisplayName("PATCH /api/v1/notifications/read-all - 전체 알림 읽음 처리 시 200을 반환한다")
    void markAllAsRead_returns200() throws Exception {
        // when & then
        mockMvc.perform(patch("/api/v1/notifications/read-all")
                        .principal(createAuthentication(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationService).markAllAsRead(1L);
    }

    private UsernamePasswordAuthenticationToken createAuthentication(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
