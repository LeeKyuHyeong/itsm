package com.itsm.api.service.admin;

import com.itsm.api.dto.admin.MenuRequest;
import com.itsm.api.dto.admin.MenuResponse;
import com.itsm.api.service.MenuCacheService;
import com.itsm.core.domain.user.Menu;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.user.MenuRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 2026-09-16 전수조사 P3 — 메뉴 관리 화면(MenuManageView)은 POST/PATCH /api/v1/admin/menus 를 부르지만
 * 백엔드에는 GET 하나뿐이라 저장이 항상 실패했다. 이 테스트가 생성·수정 서비스 계약을 고정한다.
 */
@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private MenuCacheService menuCacheService;

    @InjectMocks
    private MenuService menuService;

    private Menu menuWithId(Long id, String nm, Menu parent) {
        Menu m = Menu.builder().menuNm(nm).menuUrl("/x").sortOrder(1).parent(parent).build();
        ReflectionTestUtils.setField(m, "menuId", id);
        return m;
    }

    @Test
    @DisplayName("createMenu - 부모를 찾아 연결하고 저장한 뒤 메뉴 캐시를 비운다")
    void createMenu_savesWithParentAndEvictsCache() {
        Menu parent = menuWithId(9L, "설정관리", null);
        given(menuRepository.findById(9L)).willReturn(Optional.of(parent));
        given(menuRepository.save(any(Menu.class))).willAnswer(inv -> {
            Menu m = inv.getArgument(0);
            ReflectionTestUtils.setField(m, "menuId", 100L);
            return m;
        });

        MenuRequest req = new MenuRequest("배치 관리", "Batch Jobs", "/admin/batch-jobs", "mdi-cog", 7, 9L);
        MenuResponse res = menuService.createMenu(req);

        ArgumentCaptor<Menu> captor = ArgumentCaptor.forClass(Menu.class);
        verify(menuRepository).save(captor.capture());
        Menu saved = captor.getValue();
        assertThat(saved.getMenuNm()).isEqualTo("배치 관리");
        assertThat(saved.getMenuNmEn()).isEqualTo("Batch Jobs");
        assertThat(saved.getMenuUrl()).isEqualTo("/admin/batch-jobs");
        assertThat(saved.getIcon()).isEqualTo("mdi-cog");
        assertThat(saved.getSortOrder()).isEqualTo(7);
        assertThat(saved.getParent()).isSameAs(parent);
        assertThat(res.getMenuId()).isEqualTo(100L);
        verify(menuCacheService).evictMenuCache();
    }

    @Test
    @DisplayName("createMenu - parentId 가 없으면 최상위 메뉴로 저장한다")
    void createMenu_withoutParent_isRoot() {
        given(menuRepository.save(any(Menu.class))).willAnswer(inv -> inv.getArgument(0));

        menuService.createMenu(new MenuRequest("루트", null, null, null, 1, null));

        ArgumentCaptor<Menu> captor = ArgumentCaptor.forClass(Menu.class);
        verify(menuRepository).save(captor.capture());
        assertThat(captor.getValue().getParent()).isNull();
        verify(menuRepository, never()).findById(any());
    }

    @Test
    @DisplayName("createMenu - 존재하지 않는 부모면 ENTITY_NOT_FOUND")
    void createMenu_unknownParent_throws() {
        given(menuRepository.findById(404L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.createMenu(new MenuRequest("x", null, "/x", null, 1, 404L)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        verify(menuRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateMenu - 이름·URL·아이콘·정렬·부모를 갱신하고 캐시를 비운다")
    void updateMenu_updatesFieldsAndEvictsCache() {
        Menu existing = menuWithId(25L, "메뉴 관리", null);
        Menu newParent = menuWithId(9L, "설정관리", null);
        given(menuRepository.findById(25L)).willReturn(Optional.of(existing));
        given(menuRepository.findById(9L)).willReturn(Optional.of(newParent));

        MenuResponse res = menuService.updateMenu(25L,
                new MenuRequest("메뉴 관리2", "Menus", "/admin/menus", "mdi-menu", 3, 9L));

        assertThat(existing.getMenuNm()).isEqualTo("메뉴 관리2");
        assertThat(existing.getMenuNmEn()).isEqualTo("Menus");
        assertThat(existing.getMenuUrl()).isEqualTo("/admin/menus");
        assertThat(existing.getIcon()).isEqualTo("mdi-menu");
        assertThat(existing.getSortOrder()).isEqualTo(3);
        assertThat(existing.getParent()).isSameAs(newParent);
        assertThat(res.getMenuId()).isEqualTo(25L);
        verify(menuCacheService).evictMenuCache();
    }

    @Test
    @DisplayName("updateMenu - 자기 자신을 부모로 지정하면 INVALID_INPUT_VALUE")
    void updateMenu_selfParent_throws() {
        Menu existing = menuWithId(25L, "메뉴 관리", null);
        given(menuRepository.findById(25L)).willReturn(Optional.of(existing));

        assertThatThrownBy(() -> menuService.updateMenu(25L, new MenuRequest("x", null, "/x", null, 1, 25L)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("updateMenu - 존재하지 않는 메뉴면 ENTITY_NOT_FOUND")
    void updateMenu_unknown_throws() {
        given(menuRepository.findById(404L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.updateMenu(404L, new MenuRequest("x", null, "/x", null, 1, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }
}
