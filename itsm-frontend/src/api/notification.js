import api from './index.js'

// 백엔드 NotificationController 계약 (2026-09-16 전수조사 P5 로 정정):
// - GET  /notifications             : 내 알림 전체 목록 (페이징·필터 파라미터 없음, 응답 data 는 배열, 식별자는 notiId)
// - GET  /notifications/unread-count: 미읽음 수 (Long)
// - PATCH /notifications/{notiId}/read, /notifications/read-all
export const notificationApi = {
  getList() {
    return api.get('/notifications')
  },
  getUnreadCount() {
    return api.get('/notifications/unread-count')
  },
  markAsRead(notiId) {
    return api.patch(`/notifications/${notiId}/read`)
  },
  markAllAsRead() {
    return api.patch('/notifications/read-all')
  }
}
