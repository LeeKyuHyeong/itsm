package com.itsm.api.service.admin;

import com.itsm.api.dto.admin.MenuRequest;
import com.itsm.api.dto.admin.MenuResponse;
import com.itsm.api.service.MenuCacheService;
import com.itsm.core.domain.user.Menu;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.user.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuCacheService menuCacheService;

    public List<MenuResponse> getMenusByRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Get accessible menu IDs
        List<Long> accessibleMenuIds = menuRepository.findAccessibleMenuIds(roleIds);
        if (accessibleMenuIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Get menus
        List<Menu> menus = menuRepository.findByMenuIds(accessibleMenuIds);

        // Build tree structure
        return buildMenuTree(menus);
    }

    /**
     * 메뉴 생성 (2026-09-16 P3 — 프론트 MenuManageView 가 부르던 POST 가 백엔드에 없었다).
     * 새 메뉴는 tb_role_menu 매핑이 없으므로 SUPER_ADMIN 외에는 보이지 않는다 — 역할 매핑은 별도 기능.
     * AuthInterceptor / 사이드바가 읽는 메뉴 캐시를 즉시 비운다.
     */
    @Transactional
    public MenuResponse createMenu(MenuRequest req) {
        Menu parent = resolveParent(req.getParentId(), null);
        Menu menu = Menu.builder()
                .parent(parent)
                .menuNm(req.getName())
                .menuNmEn(req.getNameEn())
                .menuUrl(req.getPath())
                .icon(req.getIcon())
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0)
                .build();
        Menu saved = menuRepository.save(menu);
        menuCacheService.evictMenuCache();
        return toResponse(saved);
    }

    @Transactional
    public MenuResponse updateMenu(Long menuId, MenuRequest req) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "메뉴를 찾을 수 없습니다."));
        Menu parent = resolveParent(req.getParentId(), menuId);
        menu.update(parent, req.getName(), req.getNameEn(), req.getPath(), req.getIcon(),
                req.getSortOrder() != null ? req.getSortOrder() : 0);
        menuCacheService.evictMenuCache();
        return toResponse(menu);
    }

    private Menu resolveParent(Long parentId, Long selfId) {
        if (parentId == null) {
            return null;
        }
        if (parentId.equals(selfId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "메뉴를 자기 자신의 하위로 둘 수 없습니다.");
        }
        return menuRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "상위 메뉴를 찾을 수 없습니다."));
    }

    private MenuResponse toResponse(Menu menu) {
        return MenuResponse.builder()
                .menuId(menu.getMenuId())
                .menuNm(menu.getMenuNm())
                .menuNmEn(menu.getMenuNmEn())
                .menuUrl(menu.getMenuUrl())
                .icon(menu.getIcon())
                .sortOrder(menu.getSortOrder())
                .isVisible(menu.getIsVisible())
                .children(new ArrayList<>())
                .build();
    }

    private List<MenuResponse> buildMenuTree(List<Menu> menus) {
        Map<Long, MenuResponse> menuMap = new LinkedHashMap<>();
        List<MenuResponse> rootMenus = new ArrayList<>();

        // Create MenuResponse for all menus
        for (Menu menu : menus) {
            menuMap.put(menu.getMenuId(), toResponse(menu));
        }

        // Build tree
        for (Menu menu : menus) {
            MenuResponse response = menuMap.get(menu.getMenuId());
            if (menu.getParent() == null) {
                rootMenus.add(response);
            } else {
                MenuResponse parentResponse = menuMap.get(menu.getParent().getMenuId());
                if (parentResponse != null) {
                    parentResponse.getChildren().add(response);
                } else {
                    // Parent not in accessible menus, treat as root
                    rootMenus.add(response);
                }
            }
        }

        return rootMenus;
    }
}
