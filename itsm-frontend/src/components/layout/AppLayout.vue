<template>
  <div class="app-layout" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
    <AppSidebar :collapsed="sidebarCollapsed" />
    <div class="layout-main">
      <AppHeader @toggle-sidebar="toggleSidebar" />
      <main class="layout-content">
        <AppBreadcrumb />
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AppSidebar from './AppSidebar.vue'
import AppHeader from './AppHeader.vue'
import AppBreadcrumb from './AppBreadcrumb.vue'

const sidebarCollapsed = ref(false)

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
}
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
}

.layout-main {
  flex: 1;
  margin-left: var(--sidebar-width);
  display: flex;
  flex-direction: column;
  transition: margin-left 0.3s ease;
}

.app-layout.sidebar-collapsed .layout-main {
  margin-left: 60px;
}

.layout-content {
  flex: 1;
  padding: var(--spacing-lg);
  background-color: var(--color-bg);
  min-height: calc(100vh - var(--header-height));
}
</style>
