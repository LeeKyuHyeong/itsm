package com.itsm.api.controller.admin;

import com.itsm.api.dto.admin.MenuResponse;
import com.itsm.api.service.admin.MenuService;
import com.itsm.core.dto.ApiResponse;
import com.itsm.core.repository.user.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/menus")
@RequiredArgsConstructor
public class AdminMenuController {

    private final MenuService menuService;
    private final UserRoleRepository userRoleRepository;

    @GetMapping
    public ApiResponse<List<MenuResponse>> getMenus(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();

        List<Long> roleIds = userRoleRepository.findByUserId(userId).stream()
                .map(ur -> ur.getRoleId())
                .toList();

        return ApiResponse.success(menuService.getMenusByRoles(roleIds));
    }
}
