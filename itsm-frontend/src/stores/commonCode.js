import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api/index.js'

export const useCommonCodeStore = defineStore('commonCode', () => {
  // { groupCode: [{ code, name, ... }, ...] }
  const codeMap = ref({})

  function setCodes(groupCode, details) {
    codeMap.value[groupCode] = details
  }

  function getCodes(groupCode) {
    return codeMap.value[groupCode] || []
  }

  function getCodeName(groupCode, code) {
    const list = codeMap.value[groupCode] || []
    const found = list.find(item => item.code === code)
    return found?.name || code
  }

  function clearCodes() {
    codeMap.value = {}
  }

  async function fetchCodes(groupCd) {
    if (codeMap.value[groupCd]) return  // already cached
    try {
      const { data } = await api.get(`/common-codes/${groupCd}`)
      const details = data.data || data || []
      codeMap.value[groupCd] = details.map(d => ({ code: d.codeVal, name: d.codeNm }))
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
