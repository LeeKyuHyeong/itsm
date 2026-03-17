<template>
  <aside class="app-sidebar" :class="{ collapsed }">
    <div class="sidebar-brand">
      <span class="brand-icon">S</span>
      <span v-if="!collapsed" class="brand-text">ITSM</span>
    </div>

    <nav class="sidebar-nav">
      <ul class="menu-list">
        <li
          v-for="menu in menus"
          :key="menu.id"
          class="menu-item"
          :class="{ active: isMenuActive(menu), open: openMenuIds.has(menu.id) }"
        >
          <!-- Level 1 -->
          <a
            v-if="!menu.children || menu.children.length === 0"
            class="menu-link"
            :class="{ active: isRouteActive(menu.path) }"
            @click="navigateTo(menu.path)"
          >
            <span class="menu-icon"><MenuIcon :name="menu.icon" /></span>
            <span v-if="!collapsed" class="menu-text">{{ menuLabel(menu) }}</span>
          </a>

          <template v-else>
            <a class="menu-link has-children" @click="toggleMenu(menu.id)">
              <span class="menu-icon"><MenuIcon :name="menu.icon" /></span>
              <span v-if="!collapsed" class="menu-text">{{ menuLabel(menu) }}</span>
              <svg
                v-if="!collapsed"
                class="arrow-icon"
                :class="{ rotated: openMenuIds.has(menu.id) }"
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <polyline points="6 9 12 15 18 9" />
              </svg>
            </a>

            <!-- Level 2 -->
            <ul v-if="!collapsed && openMenuIds.has(menu.id)" class="submenu-list">
              <li
                v-for="child in menu.children"
                :key="child.id"
                class="submenu-item"
                :class="{ open: openMenuIds.has(child.id) }"
              >
                <a
                  v-if="!child.children || child.children.length === 0"
                  class="submenu-link"
                  :class="{ active: isRouteActive(child.path) }"
                  @click="navigateTo(child.path)"
                >
                  <span class="submenu-text">{{ menuLabel(child) }}</span>
                </a>

                <template v-else>
                  <a class="submenu-link has-children" @click="toggleMenu(child.id)">
                    <span class="submenu-text">{{ menuLabel(child) }}</span>
                    <svg
                      class="arrow-icon"
                      :class="{ rotated: openMenuIds.has(child.id) }"
                      width="14"
                      height="14"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                    >
                      <polyline points="6 9 12 15 18 9" />
                    </svg>
                  </a>

                  <!-- Level 3 -->
                  <ul v-if="openMenuIds.has(child.id)" class="submenu-list depth-3">
                    <li
                      v-for="grandchild in child.children"
                      :key="grandchild.id"
                      class="submenu-item"
                    >
                      <a
                        class="submenu-link"
                        :class="{ active: isRouteActive(grandchild.path) }"
                        @click="navigateTo(grandchild.path)"
                      >
                        <span class="submenu-text">{{ menuLabel(grandchild) }}</span>
                      </a>
                    </li>
                  </ul>
                </template>
              </li>
            </ul>
          </template>
        </li>
      </ul>
    </nav>
  </aside>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useMenuStore } from '@/stores/menu.js'
import { menuApi } from '@/api/admin/menu.js'
import MenuIcon from '@/components/layout/MenuIcon.vue'

const { locale } = useI18n()

function menuLabel(item) {
  if (locale.value === 'en' && item.nameEn) {
    return item.nameEn
  }
  return item.name
}

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const router = useRouter()
const route = useRoute()
const menuStore = useMenuStore()

const openMenuIds = reactive(new Set())

const menus = computed(() => menuStore.menus)

onMounted(async () => {
  try {
    const { data } = await menuApi.getMyMenus()
    menuStore.setMenus(data.data || [])
    // Auto-open parent menus for current route
    autoExpandCurrentRoute()
  } catch (error) {
    console.error('Menu load failed:', error)
  }
})

function autoExpandCurrentRoute() {
  const currentPath = route.path
  for (const menu of menus.value) {
    if (menu.children) {
      for (const child of menu.children) {
        if (child.path === currentPath) {
          openMenuIds.add(menu.id)
          break
        }
        if (child.children) {
          for (const grandchild of child.children) {
            if (grandchild.path === currentPath) {
              openMenuIds.add(menu.id)
              openMenuIds.add(child.id)
              break
            }
          }
        }
      }
    }
  }
}

function toggleMenu(menuId) {
  if (openMenuIds.has(menuId)) {
    openMenuIds.delete(menuId)
  } else {
    openMenuIds.add(menuId)
  }
}

function navigateTo(path) {
  if (path) {
    router.push(path)
  }
}

function isRouteActive(path) {
  return route.path === path
}

function isMenuActive(menu) {
  if (isRouteActive(menu.path)) return true
  if (menu.children) {
    return menu.children.some(child => {
      if (isRouteActive(child.path)) return true
      if (child.children) {
        return child.children.some(gc => isRouteActive(gc.path))
      }
      return false
    })
  }
  return false
}

</script>

<style scoped>
.app-sidebar {
  width: var(--sidebar-width);
  height: 100vh;
  background-color: var(--color-sidebar-bg);
  color: var(--color-sidebar-text);
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 200;
  transition: width 0.3s ease;
  overflow-y: auto;
  overflow-x: hidden;
}

.app-sidebar.collapsed {
  width: 60px;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  height: var(--header-height);
  border-bottom: 1px solid var(--color-sidebar-border);
  flex-shrink: 0;
}

.brand-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: var(--color-primary);
  color: var(--color-text-inverse);
  border-radius: var(--radius-sm);
  font-weight: 700;
  font-size: var(--font-size-lg);
  flex-shrink: 0;
}

.brand-text {
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: var(--color-text-inverse);
  letter-spacing: 1px;
}

.sidebar-nav {
  flex: 1;
  padding: var(--spacing-sm) 0;
}

.menu-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.menu-link {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 10px var(--spacing-md);
  color: var(--color-sidebar-text-muted);
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
  white-space: nowrap;
}

.menu-link:hover {
  color: var(--color-text-inverse);
  background-color: var(--color-sidebar-hover);
}

.menu-link.active {
  color: var(--color-text-inverse);
  background-color: var(--color-primary);
}

.menu-link.has-children {
  justify-content: flex-start;
}

.menu-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.menu-text {
  flex: 1;
  font-size: var(--font-size-sm);
}

.arrow-icon {
  transition: transform 0.2s;
  flex-shrink: 0;
}

.arrow-icon.rotated {
  transform: rotate(180deg);
}

.submenu-list {
  list-style: none;
  padding: 0;
  margin: 0;
  background-color: var(--color-sidebar-submenu-bg);
}

.submenu-list.depth-3 {
  background-color: var(--color-sidebar-submenu-deep-bg);
}

.submenu-link {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 8px var(--spacing-md) 8px 48px;
  color: var(--color-sidebar-text-muted);
  cursor: pointer;
  transition: all 0.2s;
  font-size: var(--font-size-sm);
  user-select: none;
  white-space: nowrap;
}

.depth-3 .submenu-link {
  padding-left: 64px;
}

.submenu-link:hover {
  color: var(--color-text-inverse);
  background-color: var(--color-sidebar-hover);
}

.submenu-link.active {
  color: var(--color-primary-light);
  background-color: var(--color-sidebar-active-bg);
}

.submenu-link.has-children {
  justify-content: flex-start;
}

.submenu-text {
  flex: 1;
}

/* Scrollbar */
.app-sidebar::-webkit-scrollbar {
  width: 4px;
}

.app-sidebar::-webkit-scrollbar-track {
  background: transparent;
}

.app-sidebar::-webkit-scrollbar-thumb {
  background-color: var(--color-scrollbar-thumb);
  border-radius: 4px;
}
</style>
