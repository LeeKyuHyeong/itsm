package com.itsm.api.controller.admin;

import com.itsm.api.aop.Auditable;
import com.itsm.api.dto.admin.MenuRequest;
import com.itsm.api.dto.admin.MenuResponse;
import com.itsm.api.service.admin.MenuService;
import com.itsm.core.constant.RoleCode;
import com.itsm.core.dto.ApiResponse;
import com.itsm.core.repository.user.UserRoleRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/menus")
@RequiredArgsConstructor
public class AdminMenuController {

    private final MenuService menuService;
    private final UserRoleRepository userRoleRepository;

    /** 로그인 사용자의 역할로 볼 수 있는 메뉴 트리 (사이드바·메뉴 관리 화면 공용) */
    @GetMapping
    public ApiResponse<List<MenuResponse>> getMenus(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<Long> roleIds = userRoleRepository.findByUserId(userId).stream()
                .map(ur -> ur.getRoleId())
                .toList();
        return ApiResponse.success(menuService.getMenusByRoles(roleIds));
    }

    // 2026-09-16 전수조사 P3: 메뉴 관리 화면(MenuManageView)이 부르던 생성/수정 엔드포인트가 없어 저장이 항상 실패했다.

    @PostMapping
    @PreAuthorize(RoleCode.HAS_ADMIN_ROLE)
    @Auditable(actionType = "CREATE", targetType = "MENU")
    public ApiResponse<MenuResponse> createMenu(@Valid @RequestBody MenuRequest req) {
        return ApiResponse.success(menuService.createMenu(req));
    }

    @PatchMapping("/{menuId}")
    @PreAuthorize(RoleCode.HAS_ADMIN_ROLE)
    @Auditable(actionType = "UPDATE", targetType = "MENU")
    public ApiResponse<MenuResponse> updateMenu(@PathVariable Long menuId, @Valid @RequestBody MenuRequest req) {
        return ApiResponse.success(menuService.updateMenu(menuId, req));
    }
}
