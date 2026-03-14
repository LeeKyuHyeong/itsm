import { defineStore } from 'pinia'
import { ref } from 'vue'

const iconMap = {
  'mdi-view-dashboard': 'dashboard',
  'mdi-alert-circle': 'incident',
  'mdi-file-document': 'service',
  'mdi-swap-horizontal': 'change',
  'mdi-server': 'asset',
  'mdi-clipboard-check': 'inspection',
  'mdi-chart-bar': 'report',
  'mdi-bulletin-board': 'board',
  'mdi-cog': 'admin',
  'mdi-account-group': 'users'
}

function mapMenu(m) {
  return {
    id: m.menuId,
    name: m.menuNm,
    nameEn: m.menuNmEn || '',
    path: m.menuUrl,
    icon: iconMap[m.icon] || m.icon,
    children: (m.children || []).map(mapMenu)
  }
}

export const useMenuStore = defineStore('menu', () => {
  const menus = ref([])

  function setMenus(menuList) {
    menus.value = (menuList || []).map(mapMenu)
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
