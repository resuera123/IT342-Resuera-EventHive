import { useState, useEffect, useRef } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { getUser, clearUser } from '../utils/auth'
import type { UserAuth } from '../utils/auth'
import { notificationsApi } from '../../features/notifications/notificationsApi'
import { isNotifEnabled } from '../../features/notifications/notificationPrefs'
import type { NotificationItem } from '../types'

interface NavbarProps {
  user?: UserAuth | null
}

const typeIcon = (type: string) => {
  switch (type) {
    case 'REGISTRATION':
      return { bg: '#d4edda', color: '#198754', icon: '✓' }
    case 'NEW_PARTICIPANT':
      return { bg: '#cff4fc', color: '#0891b2', icon: '👤' }
    case 'EVENT_CANCELLED':
      return { bg: '#f8d7da', color: '#dc3545', icon: '✕' }
    case 'EVENT_RESUMED':
      return { bg: '#d4edda', color: '#198754', icon: '▶' }
    default:
      return { bg: '#e2e3e5', color: '#6c757d', icon: '•' }
  }
}

function timeAgo(dateStr: string): string {
  const now = new Date()
  const date = new Date(dateStr)
  const seconds = Math.floor((now.getTime() - date.getTime()) / 1000)

  if (seconds < 60) return 'Just now'
  const mins = Math.floor(seconds / 60)
  if (mins < 60) return `${mins}m ago`
  const hrs = Math.floor(mins / 60)
  if (hrs < 24) return `${hrs}h ago`
  const days = Math.floor(hrs / 24)
  if (days < 7) return `${days}d ago`
  return date.toLocaleDateString()
}

export default function Navbar({ user: propUser }: NavbarProps) {
  const navigate = useNavigate()
  const location = useLocation()
  const user = propUser ?? getUser()

  const roleBadge = user?.role?.toLowerCase() === 'organizer'
    ? { label: 'Organizer', classes: 'bg-warning text-dark' }
    : { label: 'Participant', classes: 'bg-success text-white' }

  const [showNotifs, setShowNotifs] = useState(false)
  const [allNotifications, setAllNotifications] = useState<NotificationItem[]>([])
  const dropdownRef = useRef<HTMLDivElement>(null)

  // Filtered notifications based on user preferences
  const notifications = allNotifications.filter(n => isNotifEnabled(n.type))
  const unreadCount = notifications.filter(n => !n.read).length

  // Fetch notifications on mount + poll every 30s
  useEffect(() => {
    if (!user) return
    const fetchNotifs = () => {
      notificationsApi.getAll()
        .then(data => setAllNotifications(Array.isArray(data) ? data : []))
        .catch(() => {})
    }
    fetchNotifs()
    const interval = setInterval(fetchNotifs, 30000)
    return () => clearInterval(interval)
  }, [user])

  // Refetch when dropdown opens
  useEffect(() => {
    if (!showNotifs || !user) return
    notificationsApi.getAll()
      .then(data => setAllNotifications(Array.isArray(data) ? data : []))
      .catch(() => {})
  }, [showNotifs])

  // Close dropdown on outside click
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setShowNotifs(false)
      }
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  const handleMarkAllRead = () => {
    notificationsApi.markAllAsRead()
      .then(() => {
        setAllNotifications(prev => prev.map(n => ({ ...n, read: true })))
      })
      .catch(() => {})
  }

  const handleClickNotif = (notif: NotificationItem) => {
    if (!notif.read) {
      notificationsApi.markAsRead(notif.id)
        .then(() => {
          setAllNotifications(prev => prev.map(n => n.id === notif.id ? { ...n, read: true } : n))
        })
        .catch(() => {})
    }
    setShowNotifs(false)
  }

  const handleLogout = () => {
    clearUser()
    navigate('/login')
  }

  const isActive = (path: string) => location.pathname === path

  return (
    <nav className="navbar navbar-expand-lg navbar-light bg-white border-bottom shadow-sm px-4">
      <div className="container-fluid px-0">
        <a className="navbar-brand d-flex align-items-center gap-2 fw-bold" onClick={() => navigate('/dashboard')} style={{ cursor: 'pointer' }}>
          <span className="d-inline-flex align-items-center justify-content-center bg-dark text-white fw-bold rounded" style={{ width: 32, height: 32, fontSize: 11 }}>EH</span>
          EventHive
        </a>

        <div className="d-flex align-items-center gap-2 ms-auto">
          {user && <span className={`badge rounded-pill ${roleBadge.classes}`} style={{ fontSize: 11 }}>{roleBadge.label}</span>}
          <p className="mb-0 small fw-semibold">{user ? `${user.firstname} ${user.lastname}` : 'Guest'}</p>

          {/* ── Bell ── */}
          {user && (
            <div className="position-relative" ref={dropdownRef}>
              <button
                className={`btn btn-sm d-flex align-items-center justify-content-center p-2 ${showNotifs ? 'btn-secondary' : 'btn-light'}`}
                title="Notifications"
                onClick={() => setShowNotifs(!showNotifs)}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/>
                </svg>
                {unreadCount > 0 && (
                  <span className="position-absolute bg-danger rounded-circle d-flex align-items-center justify-content-center text-white" style={{ top: 2, right: 2, width: 16, height: 16, fontSize: 9, fontWeight: 700 }}>
                    {unreadCount > 9 ? '9+' : unreadCount}
                  </span>
                )}
              </button>

              {showNotifs && (
                <div className="position-absolute bg-white border rounded-3 shadow-lg" style={{ top: '100%', right: 0, width: 340, maxHeight: 420, zIndex: 1060, marginTop: 6 }}>
                  <div className="d-flex justify-content-between align-items-center px-3 py-2 border-bottom">
                    <span className="fw-bold small">Notifications</span>
                    {unreadCount > 0 && (
                      <button className="btn btn-link btn-sm text-decoration-none p-0" style={{ fontSize: 11 }} onClick={handleMarkAllRead}>Mark all read</button>
                    )}
                  </div>
                  <div style={{ maxHeight: 360, overflowY: 'auto' }}>
                    {notifications.length === 0 ? (
                      <div className="text-center py-4">
                        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#ccc" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/>
                        </svg>
                        <p className="text-muted small mt-2 mb-0">No notifications yet</p>
                      </div>
                    ) : (
                      notifications.map(notif => {
                        const ti = typeIcon(notif.type)
                        return (
                          <div key={notif.id} className="d-flex gap-2 px-3 py-2 border-bottom" style={{ cursor: 'pointer', backgroundColor: notif.read ? 'transparent' : '#f8f9ff' }} onClick={() => handleClickNotif(notif)}>
                            <div className="flex-shrink-0 d-flex align-items-center justify-content-center rounded-circle" style={{ width: 32, height: 32, backgroundColor: ti.bg, color: ti.color, fontSize: 14 }}>{ti.icon}</div>
                            <div className="flex-grow-1 overflow-hidden">
                              <div className="d-flex justify-content-between align-items-start">
                                <span className="fw-semibold" style={{ fontSize: 12 }}>{notif.title}</span>
                                {!notif.read && <span className="bg-primary rounded-circle flex-shrink-0" style={{ width: 7, height: 7, marginTop: 4 }} />}
                              </div>
                              <p className="text-muted mb-0" style={{ fontSize: 11, lineHeight: 1.3 }}>{notif.message}</p>
                              <span className="text-muted" style={{ fontSize: 10 }}>{timeAgo(notif.createdAt)}</span>
                            </div>
                          </div>
                        )
                      })
                    )}
                  </div>
                </div>
              )}
            </div>
          )}

          <button className={`btn btn-sm d-flex align-items-center justify-content-center p-2 ${isActive('/profile') ? 'btn-secondary' : 'btn-light'}`} title="Profile" onClick={() => navigate('/profile')}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/></svg>
          </button>
          <button className={`btn btn-sm d-flex align-items-center justify-content-center p-2 ${isActive('/settings') ? 'btn-secondary' : 'btn-light'}`} title="Settings" onClick={() => navigate('/settings')}>
            <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z"/></svg>
          </button>
          <button className="btn btn-sm btn-light d-flex align-items-center justify-content-center p-2" title="Logout" onClick={handleLogout}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
          </button>
        </div>
      </div>
    </nav>
  )
}