import { defineStore } from 'pinia'
import { ref } from 'vue'

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

  return {
    codeMap,
    setCodes,
    getCodes,
    getCodeName,
    clearCodes
  }
})
