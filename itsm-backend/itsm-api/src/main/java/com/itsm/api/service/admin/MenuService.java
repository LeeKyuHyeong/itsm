package com.itsm.api.service.admin;

import com.itsm.api.dto.admin.MenuResponse;
import com.itsm.core.domain.user.Menu;
import com.itsm.core.repository.user.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;

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

    private List<MenuResponse> buildMenuTree(List<Menu> menus) {
        Map<Long, MenuResponse> menuMap = new LinkedHashMap<>();
        List<MenuResponse> rootMenus = new ArrayList<>();

        // Create MenuResponse for all menus
        for (Menu menu : menus) {
            MenuResponse response = MenuResponse.builder()
                    .menuId(menu.getMenuId())
                    .menuNm(menu.getMenuNm())
                    .menuNmEn(menu.getMenuNmEn())
                    .menuUrl(menu.getMenuUrl())
                    .icon(menu.getIcon())
                    .sortOrder(menu.getSortOrder())
                    .children(new ArrayList<>())
                    .build();
            menuMap.put(menu.getMenuId(), response);
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
