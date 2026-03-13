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

// DB 메뉴명 → i18n 키 매핑
const menuI18nMap = {
  '대시보드': 'menu.dashboard',
  '장애관리': 'menu.incident',
  '장애 목록': 'menu.incidentList',
  '장애 등록': 'menu.incidentCreate',
  '서비스요청': 'menu.serviceRequest',
  '요청 목록': 'menu.srList',
  '요청 등록': 'menu.srCreate',
  '변경관리': 'menu.change',
  '변경 목록': 'menu.changeList',
  '변경 등록': 'menu.changeCreate',
  '자산관리': 'menu.asset',
  'HW 자산 목록': 'menu.hwAssetList',
  'SW 자산 목록': 'menu.swAssetList',
  '정기점검관리': 'menu.inspection',
  '점검 목록': 'menu.inspectionList',
  '점검 등록': 'menu.inspectionCreate',
  '보고관리': 'menu.report',
  '보고서 목록': 'menu.reportList',
  '게시판': 'menu.board',
  '설정관리': 'menu.settings',
  '메뉴 관리': 'menu.menuManage',
  '공통코드 관리': 'menu.commonCode',
  '게시판 관리': 'menu.boardManage',
  'SLA 관리': 'menu.slaManage',
  '알림 정책 관리': 'menu.notificationPolicy',
  '계정관리': 'menu.account',
  '사용자 관리': 'menu.accountManage',
  '조직 관리': 'menu.orgManage'
}

function mapMenu(m) {
  return {
    id: m.menuId,
    name: m.menuNm,
    i18nKey: menuI18nMap[m.menuNm] || '',
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
