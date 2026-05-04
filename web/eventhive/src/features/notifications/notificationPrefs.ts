// Single source of truth for notification preferences
// Used by Navbar (filtering displayed notifications) and SettingsPage (editing prefs)

const PREFS_KEY = 'eventhive_notif_prefs'

export interface NotificationPrefs {
  registration: boolean
  newParticipant: boolean
  cancellation: boolean
  eventUpdates: boolean
}

const DEFAULTS: NotificationPrefs = {
  registration: true,
  newParticipant: true,
  cancellation: true,
  eventUpdates: true,
}

// Map notification type → preference key
export const typeToPrefKey: Record<string, keyof NotificationPrefs> = {
  REGISTRATION: 'registration',
  NEW_PARTICIPANT: 'newParticipant',
  EVENT_CANCELLED: 'cancellation',
  EVENT_RESUMED: 'eventUpdates',
}

export function getNotificationPrefs(): NotificationPrefs {
  try {
    const stored = localStorage.getItem(PREFS_KEY)
    if (stored) return { ...DEFAULTS, ...JSON.parse(stored) }
  } catch {}
  return DEFAULTS
}

export function saveNotificationPrefs(prefs: NotificationPrefs) {
  localStorage.setItem(PREFS_KEY, JSON.stringify(prefs))
}

export function isNotifEnabled(type: string): boolean {
  const prefs = getNotificationPrefs()
  const key = typeToPrefKey[type]
  if (!key) return true
  return prefs[key] !== false
}