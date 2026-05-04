import { apiFetch } from '../../shared/api/client'
import type { EventItem } from '../../shared/types'

export interface EventRequest {
  title: string
  description: string
  startDate: string
  endDate: string
  location: string
  category: string
  maxParticipants: number
}

export const eventsApi = {
  getAll: () =>
    apiFetch<EventItem[]>('/api/events'),

  getMyEvents: () =>
    apiFetch<EventItem[]>('/api/events/my-events'),

  getRegistered: () =>
    apiFetch<EventItem[]>('/api/events/registered'),

  getByCategory: (category: string) =>
    apiFetch<EventItem[]>(`/api/events/category?category=${encodeURIComponent(category)}`),

  // JSON create (without image)
  create: (data: EventRequest) =>
    apiFetch<EventItem>('/api/events', {
      method: 'POST',
      body: data,
    }),

  // Multipart create (with optional image)
  createWithImage: (data: EventRequest, image?: File) => {
    const formData = new FormData()
    formData.append('title', data.title)
    formData.append('description', data.description)
    formData.append('startDate', data.startDate)
    formData.append('endDate', data.endDate)
    formData.append('location', data.location)
    formData.append('category', data.category)
    formData.append('maxParticipants', String(data.maxParticipants))
    if (image) formData.append('image', image)

    return apiFetch<EventItem>('/api/events', {
      method: 'POST',
      body: formData,
    })
  },

  update: (id: number, data: EventRequest, image?: File) => {
    const formData = new FormData()
    formData.append('title', data.title)
    formData.append('description', data.description)
    formData.append('startDate', data.startDate)
    formData.append('endDate', data.endDate)
    formData.append('location', data.location)
    formData.append('category', data.category)
    formData.append('maxParticipants', String(data.maxParticipants))
    if (image) formData.append('image', image)

    return apiFetch<EventItem>(`/api/events/${id}`, {
      method: 'PUT',
      body: formData,
    })
  },

  updateStatus: (id: number, status: string) =>
    apiFetch<EventItem>(`/api/events/${id}/status?status=${status}`, {
      method: 'PATCH',
    }),

  delete: (id: number) =>
    apiFetch<void>(`/api/events/${id}`, {
      method: 'DELETE',
    }),

  register: (id: number) =>
    apiFetch<EventItem>(`/api/events/${id}/register`, {
      method: 'POST',
    }),
}