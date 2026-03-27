package com.itsm.api.config;

import com.itsm.api.service.MenuCacheService;
import com.itsm.core.domain.user.Menu;
import com.itsm.core.repository.user.MenuRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(CacheConfigTest.TestConfig.class)
class CacheConfigTest {

    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        public MenuRepository menuRepository() {
            return mock(MenuRepository.class);
        }

        @Bean
        public MenuCacheService menuCacheService(MenuRepository menuRepository) {
            return new MenuCacheService(menuRepository);
        }

        @Bean
        public CacheManager cacheManager() {
            CaffeineCacheManager cacheManager = new CaffeineCacheManager("menus");
            cacheManager.setCaffeine(Caffeine.newBuilder()
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .maximumSize(100));
            return cacheManager;
        }
    }

    @Autowired
    private MenuCacheService menuCacheService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private MenuRepository menuRepository;

    @BeforeEach
    void setUp() {
        reset(menuRepository);
        cacheManager.getCache("menus").clear();
    }

    @Test
    @DisplayName("CacheManager가 정상적으로 등록된다")
    void shouldHaveCacheManager() {
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getCacheNames()).contains("menus");
    }

    @Test
    @DisplayName("getAllMenus는 캐시를 사용하여 반복 호출 시 DB를 한 번만 조회한다")
    void shouldCacheMenusAndAvoidRepeatedDbCalls() {
        // given
        Menu menu = Menu.builder()
                .menuNm("사용자 관리")
                .menuUrl("/api/v1/users/**")
                .sortOrder(1)
                .build();
        ReflectionTestUtils.setField(menu, "menuId", 1L);
        given(menuRepository.findAll()).willReturn(List.of(menu));

        // when - 동일 메서드 2번 호출
        List<Menu> first = menuCacheService.getAllMenus();
        List<Menu> second = menuCacheService.getAllMenus();

        // then - 캐시 적용으로 DB는 1번만 호출
        verify(menuRepository, times(1)).findAll();
        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("evictMenuCache 호출 후에는 DB를 다시 조회한다")
    void shouldEvictCacheAndRefetch() {
        // given
        Menu menu = Menu.builder()
                .menuNm("사용자 관리")
                .menuUrl("/api/v1/users/**")
                .sortOrder(1)
                .build();
        ReflectionTestUtils.setField(menu, "menuId", 1L);
        given(menuRepository.findAll()).willReturn(List.of(menu));

        // when
        menuCacheService.getAllMenus();       // 1번째 DB 호출
        menuCacheService.evictMenuCache();    // 캐시 삭제
        menuCacheService.getAllMenus();       // 2번째 DB 호출

        // then
        verify(menuRepository, times(2)).findAll();
    }
}
