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
            <span class="menu-icon" v-html="getMenuIcon(menu.icon)"></span>
            <span v-if="!collapsed" class="menu-text">{{ menu.name }}</span>
          </a>

          <template v-else>
            <a class="menu-link has-children" @click="toggleMenu(menu.id)">
              <span class="menu-icon" v-html="getMenuIcon(menu.icon)"></span>
              <span v-if="!collapsed" class="menu-text">{{ menu.name }}</span>
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
                  <span class="submenu-text">{{ child.name }}</span>
                </a>

                <template v-else>
                  <a class="submenu-link has-children" @click="toggleMenu(child.id)">
                    <span class="submenu-text">{{ child.name }}</span>
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
                        <span class="submenu-text">{{ grandchild.name }}</span>
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
import { useMenuStore } from '@/stores/menu.js'
import { menuApi } from '@/api/admin/menu.js'

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
    console.error('메뉴 로드 실패:', error)
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

function getMenuIcon(icon) {
  const icons = {
    dashboard: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>',
    incident: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>',
    service: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>',
    change: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>',
    asset: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>',
    inspection: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>',
    board: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>',
    report: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>',
    admin: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>',
    users: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>',
    org: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>',
  }
  return icons[icon] || icons['dashboard']
}
</script>

<style scoped>
.app-sidebar {
  width: var(--sidebar-width);
  height: 100vh;
  background-color: #1e293b;
  color: #cbd5e1;
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
  border-bottom: 1px solid #334155;
  flex-shrink: 0;
}

.brand-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: var(--color-primary);
  color: white;
  border-radius: var(--radius-sm);
  font-weight: 700;
  font-size: var(--font-size-lg);
  flex-shrink: 0;
}

.brand-text {
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: white;
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
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
  white-space: nowrap;
}

.menu-link:hover {
  color: white;
  background-color: #334155;
}

.menu-link.active {
  color: white;
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
  background-color: #162032;
}

.submenu-list.depth-3 {
  background-color: #0f172a;
}

.submenu-link {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 8px var(--spacing-md) 8px 48px;
  color: #94a3b8;
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
  color: white;
  background-color: #334155;
}

.submenu-link.active {
  color: var(--color-primary-light);
  background-color: rgba(26, 115, 232, 0.1);
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
  background-color: #475569;
  border-radius: 4px;
}
</style>
