<template>
  <nav v-if="breadcrumbs.length > 0" class="app-breadcrumb">
    <ol class="breadcrumb-list">
      <li
        v-for="(crumb, index) in breadcrumbs"
        :key="index"
        class="breadcrumb-item"
        :class="{ active: index === breadcrumbs.length - 1 }"
      >
        <router-link
          v-if="crumb.path && index < breadcrumbs.length - 1"
          :to="crumb.path"
          class="breadcrumb-link"
        >
          {{ crumb.title }}
        </router-link>
        <span v-else class="breadcrumb-text">{{ crumb.title }}</span>

        <svg
          v-if="index < breadcrumbs.length - 1"
          class="breadcrumb-separator"
          width="14"
          height="14"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
        >
          <polyline points="9 18 15 12 9 6" />
        </svg>
      </li>
    </ol>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const breadcrumbs = computed(() => {
  const crumbs = []
  const matched = route.matched

  for (const record of matched) {
    const title = record.meta?.title || record.meta?.breadcrumb || record.name
    if (title && title !== 'Home') {
      crumbs.push({
        title,
        path: record.path
      })
    }
  }

  // Use route meta breadcrumb array if provided
  if (route.meta?.breadcrumbs && Array.isArray(route.meta.breadcrumbs)) {
    return route.meta.breadcrumbs
  }

  return crumbs
})
</script>

<style scoped>
.app-breadcrumb {
  padding: var(--spacing-sm) 0;
  margin-bottom: var(--spacing-md);
}

.breadcrumb-list {
  list-style: none;
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: 0;
  margin: 0;
}

.breadcrumb-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-size-sm);
}

.breadcrumb-link {
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: color 0.2s;
}

.breadcrumb-link:hover {
  color: var(--color-primary);
}

.breadcrumb-item.active .breadcrumb-text {
  color: var(--color-text);
  font-weight: 500;
}

.breadcrumb-separator {
  color: var(--color-text-disabled);
}
</style>
