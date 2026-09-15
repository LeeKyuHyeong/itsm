package com.itsm.api.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.api.dto.admin.MenuRequest;
import com.itsm.api.dto.admin.MenuResponse;
import com.itsm.api.exception.GlobalExceptionHandler;
import com.itsm.api.service.admin.MenuService;
import com.itsm.core.repository.user.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 프론트 api/admin/menu.js 가 부르는 POST /api/v1/admin/menus, PATCH /api/v1/admin/menus/{id} 계약 (2026-09-16 P3).
 */
@ExtendWith(MockitoExtension.class)
class AdminMenuControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MenuService menuService;

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private AdminMenuController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/admin/menus - 메뉴를 생성하고 menuId 를 돌려준다")
    void createMenu_returns200WithId() throws Exception {
        given(menuService.createMenu(any(MenuRequest.class)))
                .willReturn(MenuResponse.builder().menuId(100L).menuNm("배치 관리").build());

        mockMvc.perform(post("/api/v1/admin/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new MenuRequest("배치 관리", "Batch", "/admin/batch-jobs", "mdi-cog", 7, 9L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.menuId").value(100));
    }

    @Test
    @DisplayName("POST /api/v1/admin/menus - 이름이 비면 400")
    void createMenu_blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/admin/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new MenuRequest("", null, "/x", null, 1, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/menus/{menuId} - 메뉴를 수정한다")
    void updateMenu_returns200() throws Exception {
        given(menuService.updateMenu(eq(25L), any(MenuRequest.class)))
                .willReturn(MenuResponse.builder().menuId(25L).menuNm("메뉴 관리2").build());

        mockMvc.perform(patch("/api/v1/admin/menus/25")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new MenuRequest("메뉴 관리2", null, "/admin/menus", null, 3, 9L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menuNm").value("메뉴 관리2"));
    }
}
