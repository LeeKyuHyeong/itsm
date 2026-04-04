import { getLocale } from '@/i18n/index.js'

const LOCALE_MAP = {
  ko: 'ko-KR',
  en: 'en-US'
}

function resolveLocale() {
  return LOCALE_MAP[getLocale()] || 'ko-KR'
}

/**
 * 날짜+시간 포맷 (locale 기반)
 * @param {string|Date} dateStr
 * @returns {string}
 */
export function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString(resolveLocale(), {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

/**
 * 날짜만 포맷 (시간 제외, locale 기반)
 * @param {string|Date} dateStr
 * @returns {string}
 */
export function formatDateShort(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString(resolveLocale(), {
    year: 'numeric', month: '2-digit', day: '2-digit'
  })
}
