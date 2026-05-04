import { apiFetch } from '../../shared/api/client'
import type { NotificationItem } from '../../shared/types'

export const notificationsApi = {
  getAll: () =>
    apiFetch<NotificationItem[]>('/api/notifications'),

  getUnreadCount: () =>
    apiFetch<{ count: number }>('/api/notifications/unread-count'),

  markAsRead: (id: number) =>
    apiFetch<void>(`/api/notifications/${id}/read`, { method: 'PATCH' }),

  markAllAsRead: () =>
    apiFetch<void>('/api/notifications/read-all', { method: 'PATCH' }),
}