import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useMenuStore = defineStore('menu', () => {
  const menus = ref([])

  function setMenus(menuList) {
    menus.value = menuList
  }

  function clearMenus() {
    menus.value = []
  }

  return {
    menus,
    setMenus,
    clearMenus
  }
})
