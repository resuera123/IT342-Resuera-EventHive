import { useState } from 'react'
import type { UserAuth } from '../../shared/utils/auth'

type EventItem = {
  id: number
  title: string
  description: string
  startDate: string
  endDate: string
  location: string
  category: string
  imageUrl?: string
  maxParticipants?: number
  status: string
  organizerId: number
  organizerName: string
  createdAt: string
  participantCount: number
  isRegistered?: boolean
}

interface EventDetailModalProps {
  event: EventItem | null
  user: UserAuth | null
  onClose: () => void
  onRegistered: () => void
}

const categoryBadge = (cat: string) => {
  const map: Record<string, string> = {
    Music: 'bg-purple',
    Sports: 'bg-sports',
    Tech: 'bg-info text-dark',
    Arts: 'bg-arts',
    'Food & Drink': 'bg-food',
    Business: 'bg-business',
    Health: 'bg-health',
  }
  return map[cat] ?? 'bg-secondary'
}

const statusBadge = (status: string) => {
  switch (status) {
    case 'UPCOMING': return 'bg-primary'
    case 'ONGOING': return 'bg-success'
    case 'COMPLETED': return 'bg-secondary'
    case 'CANCELLED': return 'bg-danger'
    default: return 'bg-secondary'
  }
}

// Inline styles for custom badge colors
const badgeStyle = (cls: string): React.CSSProperties => {
  const styles: Record<string, React.CSSProperties> = {
    'bg-purple': { backgroundColor: '#7c3aed', color: '#fff' },
    'bg-sports': { backgroundColor: '#ea580c', color: '#fff' },
    'bg-arts': { backgroundColor: '#db2777', color: '#fff' },
    'bg-food': { backgroundColor: '#d97706', color: '#fff' },
    'bg-business': { backgroundColor: '#1e40af', color: '#fff' },
    'bg-health': { backgroundColor: '#059669', color: '#fff' },
  }
  return styles[cls] ?? {}
}

export default function EventDetailModal({ event, user, onClose, onRegistered }: EventDetailModalProps) {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [showSuccess, setShowSuccess] = useState(false)

  if (!event) return null

  const isOrganizer = user?.id === event.organizerId
  const isRegistered = event.isRegistered === true
  const isFull = event.maxParticipants != null && event.participantCount >= event.maxParticipants
  const isCancelled = event.status === 'CANCELLED'
  const canRegister = !isOrganizer && !isRegistered && !isFull && !isCancelled && user != null

  const handleRegister = async () => {
    setLoading(true)
    setError('')
    try {
      const res = await fetch(`http://localhost:8081/api/events/${event.id}/register`, {
        method: 'POST',
        credentials: 'include',
      })
      if (res.ok) {
        setShowSuccess(true)
      } else {
        const data = await res.json()
        setError(data.message || 'Registration failed')
      }
    } catch {
      setError('Unable to connect to server.')
    } finally {
      setLoading(false)
    }
  }

  const handleSuccessClose = () => {
    setShowSuccess(false)
    onRegistered()
    onClose()
  }

  // ── Success confirmation modal ──
  if (showSuccess) {
    return (
      <>
        <div className="modal-backdrop fade show" style={{ zIndex: 1040 }} onClick={handleSuccessClose} />
        <div className="modal fade show d-block" style={{ zIndex: 1050 }} role="dialog">
          <div className="modal-dialog modal-dialog-centered modal-sm">
            <div className="modal-content border-0 shadow text-center p-4">
              <div className="mx-auto mb-3" style={{ width: 64, height: 64, borderRadius: '50%', background: '#d4edda' }}>
                <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
                  <circle cx="32" cy="32" r="32" fill="#d4edda"/>
                  <path d="M20 33l8 8 16-16" stroke="#198754" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </div>
              <h5 className="fw-bold mb-2">Registration Successful!</h5>
              <p className="text-muted small mb-3">
                You have successfully registered for <strong>{event.title}</strong>. This event has been added to your event list.
              </p>
              <button className="btn btn-success btn-sm w-100" onClick={handleSuccessClose}>
                Done
              </button>
            </div>
          </div>
        </div>
      </>
    )
  }

  // ── Main detail modal ──
  const buttonLabel = isOrganizer
    ? 'Your Event'
    : isCancelled
      ? 'Event Cancelled'
      : isRegistered
        ? 'Already Registered'
        : isFull
          ? 'Event Full'
          : 'Register for Event'

  const catCls = categoryBadge(event.category)
  const stsCls = statusBadge(event.status)

  return (
    <>
      <div className="modal-backdrop fade show" style={{ zIndex: 1040 }} onClick={onClose} />
      <div className="modal fade show d-block" style={{ zIndex: 1050 }} role="dialog">
        <div className="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
          <div className="modal-content border-0 shadow">

            {/* Header image */}
            {event.imageUrl && (
              <img
                src={`http://localhost:8081${event.imageUrl}`}
                alt={event.title}
                className="w-100"
                style={{ maxHeight: 220, objectFit: 'cover' }}
              />
            )}

            <div className="modal-body p-4">
              {/* Badges + close */}
              <div className="d-flex justify-content-between align-items-start mb-2">
                <div className="d-flex gap-1">
                  <span className={`badge ${stsCls}`}>{event.status}</span>
                  <span className="badge" style={badgeStyle(catCls)}>{event.category}</span>
                </div>
                <button type="button" className="btn-close" onClick={onClose} />
              </div>

              {/* Title */}
              <h4 className="fw-bold mb-1">{event.title}</h4>
              <p className="text-muted small mb-3">Organized by {event.organizerName}</p>

              {/* Description */}
              <p className="mb-3">{event.description}</p>

              {/* Details grid */}
              <div className="row g-3 mb-3">
                <div className="col-6">
                  <div className="d-flex align-items-center gap-2">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                      <line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/>
                      <line x1="3" y1="10" x2="21" y2="10"/>
                    </svg>
                    <div>
                      <div className="text-muted" style={{ fontSize: 11 }}>Date</div>
                      <div className="small fw-medium">
                        {new Date(event.startDate).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })}
                      </div>
                    </div>
                  </div>
                </div>
                <div className="col-6">
                  <div className="d-flex align-items-center gap-2">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                    </svg>
                    <div>
                      <div className="text-muted" style={{ fontSize: 11 }}>Time</div>
                      <div className="small fw-medium">
                        {new Date(event.startDate).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        {' – '}
                        {new Date(event.endDate).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </div>
                    </div>
                  </div>
                </div>
                <div className="col-6">
                  <div className="d-flex align-items-center gap-2">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/>
                    </svg>
                    <div>
                      <div className="text-muted" style={{ fontSize: 11 }}>Location</div>
                      <div className="small fw-medium">{event.location}</div>
                    </div>
                  </div>
                </div>
                <div className="col-6">
                  <div className="d-flex align-items-center gap-2">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/>
                      <path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/>
                    </svg>
                    <div>
                      <div className="text-muted" style={{ fontSize: 11 }}>Attendance</div>
                      <div className="small fw-medium">
                        {event.participantCount} / {event.maxParticipants ?? '∞'} registered
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Capacity bar */}
              {event.maxParticipants != null && (
                <div className="progress mb-3" style={{ height: 6 }}>
                  <div
                    className={`progress-bar ${isFull ? 'bg-danger' : 'bg-success'}`}
                    style={{ width: `${Math.min(100, (event.participantCount / event.maxParticipants) * 100)}%` }}
                  />
                </div>
              )}

              {error && (
                <div className="alert alert-danger py-2 small">{error}</div>
              )}
            </div>

            {/* Footer */}
            <div className="modal-footer border-0 pt-0 px-4 pb-4">
              <button className="btn btn-sm btn-outline-secondary" onClick={onClose}>
                Close
              </button>
              <button
                className={`btn btn-sm ${canRegister ? 'btn-success' : 'btn-secondary'}`}
                disabled={!canRegister || loading}
                onClick={handleRegister}
              >
                {loading ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true" />
                    Registering...
                  </>
                ) : buttonLabel}
              </button>
            </div>

          </div>
        </div>
      </div>
    </>
  )
}