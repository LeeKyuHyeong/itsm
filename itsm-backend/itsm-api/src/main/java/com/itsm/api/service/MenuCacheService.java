package com.itsm.api.service;

import com.itsm.core.domain.user.Menu;
import com.itsm.core.repository.user.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuCacheService {

    private final MenuRepository menuRepository;

    @Cacheable(value = "menus")
    public List<Menu> getAllMenus() {
        return menuRepository.findAll();
    }

    @CacheEvict(value = "menus", allEntries = true)
    public void evictMenuCache() {
        // 캐시 무효화
    }
}
