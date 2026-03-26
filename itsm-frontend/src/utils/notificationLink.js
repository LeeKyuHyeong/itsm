/**
 * refType → 내부 라우터 경로 매핑 (화이트리스트)
 * SIMULATION 등 네비게이션 대상이 없는 타입은 의도적으로 제외
 */
const REF_TYPE_PATH_MAP = {
  INCIDENT: '/incidents',
  SERVICE_REQUEST: '/service-requests',
  CHANGE: '/changes',
  INSPECTION: '/inspections',
  ASSET_HW: '/assets/hw',
  ASSET_SW: '/assets/sw',
  ASSET_OA: '/assets/oa'
}

/**
 * refType과 refId로부터 안전한 내부 라우터 경로를 생성한다.
 * 허용되지 않은 refType이나 유효하지 않은 refId는 null을 반환하여 리다이렉트를 차단한다.
 *
 * @param {string|null|undefined} refType
 * @param {number|string|null|undefined} refId
 * @returns {string|null}
 */
export function buildSafeRefLink(refType, refId) {
  if (!refType || !refId) return null

  const basePath = REF_TYPE_PATH_MAP[refType]
  if (!basePath) return null

  const id = Number(refId)
  if (!Number.isInteger(id) || id <= 0) return null

  return `${basePath}/${id}`
}
