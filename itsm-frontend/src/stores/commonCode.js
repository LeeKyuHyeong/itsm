import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import api from '@/api/index.js'

export const useCommonCodeStore = defineStore('commonCode', () => {
  const { locale } = useI18n()

  // { groupCode: [{ code, name, nameEn, ... }, ...] }
  const codeMap = ref({})

  function setCodes(groupCode, details) {
    codeMap.value[groupCode] = details
  }

  function getCodes(groupCode) {
    const list = codeMap.value[groupCode] || []
    if (locale.value === 'en') {
      return list.map(item => ({
        ...item,
        name: item.nameEn || item.name
      }))
    }
    return list
  }

  function getCodeName(groupCode, code) {
    const list = codeMap.value[groupCode] || []
    const found = list.find(item => item.code === code)
    if (!found) return code
    if (locale.value === 'en' && found.nameEn) return found.nameEn
    return found.name || code
  }

  function clearCodes() {
    codeMap.value = {}
  }

  async function fetchCodes(groupCd) {
    if (codeMap.value[groupCd]) return  // already cached
    try {
      const { data } = await api.get(`/common-codes/${groupCd}`)
      const details = data.data || data || []
      codeMap.value[groupCd] = details.map(d => ({
        code: d.codeVal,
        name: d.codeNm,
        nameEn: d.codeNmEn || ''
      }))
    } catch (e) {
      console.error(`공통코드 로드 실패: ${groupCd}`, e)
    }
  }

  return {
    codeMap,
    setCodes,
    getCodes,
    getCodeName,
    clearCodes,
    fetchCodes
  }
})
