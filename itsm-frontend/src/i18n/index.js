import { createI18n } from 'vue-i18n'
import ko from './locales/ko.js'
import en from './locales/en.js'

const savedLocale = localStorage.getItem('itsm-locale') || 'ko'

const i18n = createI18n({
  legacy: false,
  locale: savedLocale,
  fallbackLocale: 'ko',
  messages: { ko, en }
})

export default i18n

export function setLocale(locale) {
  i18n.global.locale.value = locale
  localStorage.setItem('itsm-locale', locale)
}

export function getLocale() {
  return i18n.global.locale.value
}
