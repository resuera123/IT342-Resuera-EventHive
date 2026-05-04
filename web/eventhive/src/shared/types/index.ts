// Shared TypeScript types used across features

export interface UserAuth {
  id: number
  firstname: string
  lastname: string
  email: string
  role: string
  createdAt: string
  profilePicUrl?: string
}

export interface EventItem {
  id: number
  title: string
  description: string
  startDate: string
  endDate: string
  location: string
  category: string
  imageUrl?: string
  maxParticipants: number
  status: 'UPCOMING' | 'ONGOING' | 'COMPLETED' | 'CANCELLED'
  organizerId: number
  organizerName: string
  createdAt: string
  participantCount: number
  isRegistered?: boolean | null
}

export interface NotificationItem {
  id: number
  type: 'REGISTRATION' | 'NEW_PARTICIPANT' | 'EVENT_CANCELLED' | 'EVENT_RESUMED'
  title: string
  message: string
  read: boolean
  eventId: number
  createdAt: string
}