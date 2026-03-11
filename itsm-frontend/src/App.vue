<template>
  <AppLayout v-if="isAuthenticated && !isAuthPage">
    <router-view />
  </AppLayout>
  <router-view v-else />
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth.js'
import AppLayout from '@/components/layout/AppLayout.vue'

const route = useRoute()
const authStore = useAuthStore()

const isAuthenticated = computed(() => authStore.isAuthenticated)

const isAuthPage = computed(() => {
  const authPages = ['Login', 'ChangePassword']
  return authPages.includes(route.name)
})
</script>
