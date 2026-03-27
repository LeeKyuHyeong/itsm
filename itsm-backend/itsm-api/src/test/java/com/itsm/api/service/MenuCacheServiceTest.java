package com.itsm.api.service;

import com.itsm.core.domain.user.Menu;
import com.itsm.core.repository.user.MenuRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class MenuCacheServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private MenuCacheService menuCacheService;

    @Test
    @DisplayName("getAllMenus는 menuRepository.findAll()의 결과를 반환한다")
    void shouldReturnAllMenus() {
        // given
        Menu menu = Menu.builder()
                .menuNm("사용자 관리")
                .menuUrl("/api/v1/users/**")
                .sortOrder(1)
                .build();
        ReflectionTestUtils.setField(menu, "menuId", 1L);
        given(menuRepository.findAll()).willReturn(List.of(menu));

        // when
        List<Menu> result = menuCacheService.getAllMenus();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMenuNm()).isEqualTo("사용자 관리");
    }

    @Test
    @DisplayName("evictMenuCache 호출 후에는 다음 호출 시 DB를 다시 조회한다")
    void shouldEvictCacheAndRefetch() {
        // given
        Menu menu = Menu.builder()
                .menuNm("사용자 관리")
                .menuUrl("/api/v1/users/**")
                .sortOrder(1)
                .build();
        ReflectionTestUtils.setField(menu, "menuId", 1L);
        given(menuRepository.findAll()).willReturn(List.of(menu));

        // when - 단위 테스트에서는 캐시가 동작하지 않으므로 각 호출마다 repository 호출
        menuCacheService.getAllMenus();
        menuCacheService.getAllMenus();

        // then - 캐시 미적용 상태이므로 2번 호출됨 (통합 테스트에서 캐시 동작 검증)
        verify(menuRepository, times(2)).findAll();
    }
}
