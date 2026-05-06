import { useState, useEffect, useRef } from 'react'
import type { ChangeEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { getUser, saveUser } from '../../shared/utils/auth.ts'
import type { UserAuth } from '../../shared/utils/auth.ts'
import { resolveImageUrl } from '../../shared/utils/images'
import Navbar from '../../shared/components/Navbar.tsx'
import EditEventModal from '../events/EditEventModal.tsx'
import { eventsApi } from '../events/eventsApi.ts'
import { settingsApi } from '../settings/settingsApi.ts'

interface EventItem {
  id: number
  title: string
  description: string
  startDate: string
  endDate: string
  location: string
  category: string
  imageUrl?: string
  status: string
  participantCount?: number
  maxParticipants?: number
}

const statusBadgeColor = (s: string) => {
  switch (s) {
    case 'UPCOMING': return 'bg-primary'
    case 'ONGOING': return 'bg-success'
    case 'COMPLETED': return 'bg-secondary'
    case 'CANCELLED': return 'bg-danger'
    default: return 'bg-secondary'
  }
}

const catColor = (c: string): React.CSSProperties => {
  const map: Record<string, React.CSSProperties> = {
    Music:          { backgroundColor: '#7c3aed', color: '#fff' },
    Sports:         { backgroundColor: '#ea580c', color: '#fff' },
    Tech:           { backgroundColor: '#0891b2', color: '#fff' },
    Arts:           { backgroundColor: '#db2777', color: '#fff' },
    'Food & Drink': { backgroundColor: '#d97706', color: '#fff' },
    Business:       { backgroundColor: '#1e40af', color: '#fff' },
    Health:         { backgroundColor: '#059669', color: '#fff' },
    Technology:     { backgroundColor: '#0891b2', color: '#fff' },
    Workshop:       { backgroundColor: '#7c3aed', color: '#fff' },
  }
  return map[c] ?? { backgroundColor: '#6c757d', color: '#fff' }
}

function capitalize(s: string): string {
  return s.charAt(0).toUpperCase() + s.slice(1).toLowerCase()
}

type TabKey = 'own' | 'my' | 'past'

type ModalAction = {
  type: 'cancel' | 'delete' | 'continue'
  event: EventItem
} | null

type SuccessModal = { title: string; message: string } | null

export default function ProfilePage() {
  const navigate = useNavigate()
  const [user, setUser] = useState<UserAuth | null>(getUser())
  const isOrganizer = user?.role?.toLowerCase() === 'organizer'

  const [activeTab, setActiveTab] = useState<TabKey>(isOrganizer ? 'own' : 'my')
  const [registeredEvents, setRegisteredEvents] = useState<EventItem[]>([])
  const [ownEvents, setOwnEvents] = useState<EventItem[]>([])
  const [loading, setLoading] = useState(true)
  const [actionLoading, setActionLoading] = useState(false)
  const [modal, setModal] = useState<ModalAction>(null)
  const [editingEvent, setEditingEvent] = useState<EventItem | null>(null)

  // ── Profile picture state ──
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [picUploading, setPicUploading] = useState(false)
  const [successModal, setSuccessModal] = useState<SuccessModal>(null)
  const [picError, setPicError] = useState<string | null>(null)

  const fetchData = () => {
    const fetches: Promise<void>[] = []
    fetches.push(
      eventsApi.getRegistered()
        .then(data => setRegisteredEvents(Array.isArray(data) ? data as EventItem[] : []))
        .catch(err => console.error('Failed to load registered events', err))
    )
    if (isOrganizer) {
      fetches.push(
        eventsApi.getMyEvents()
          .then(data => setOwnEvents(Array.isArray(data) ? data as EventItem[] : []))
          .catch(err => console.error('Failed to load own events', err))
      )
    }
    return Promise.all(fetches)
  }

  useEffect(() => { fetchData().finally(() => setLoading(false)) }, [isOrganizer])

  const handleAction = async () => {
    if (!modal) return
    setActionLoading(true)
    try {
      if (modal.type === 'cancel') {
        await eventsApi.updateStatus(modal.event.id, 'CANCELLED')
      } else if (modal.type === 'continue') {
        await eventsApi.updateStatus(modal.event.id, 'UPCOMING')
      } else if (modal.type === 'delete') {
        await eventsApi.delete(modal.event.id)
      }
      await fetchData()
    } catch {
      alert('Unable to connect to server')
    } finally {
      setActionLoading(false)
      setModal(null)
    }
  }

  // ── Profile picture upload handlers ──

  const handleAvatarClick = () => {
    if (picUploading) return
    fileInputRef.current?.click()
  }

  const handleFileChange = async (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    e.target.value = '' // reset input so same file can be re-selected later
    if (!file) return

    if (!file.type.startsWith('image/')) {
      setPicError('Please select an image file.')
      setTimeout(() => setPicError(null), 4000)
      return
    }
    if (file.size > 5 * 1024 * 1024) {
      setPicError('Image must be smaller than 5 MB.')
      setTimeout(() => setPicError(null), 4000)
      return
    }

    setPicUploading(true)
    setPicError(null)
    try {
      const result = await settingsApi.uploadProfilePic(file)
      const newUrl = result.profilePicUrl

      // Persist updated user to localStorage so other pages see the new pic
      if (user) {
        const updated: UserAuth = { ...user, profilePicUrl: newUrl }
        saveUser(updated)
        setUser(updated)
      }

      setSuccessModal({
        title: 'Photo Updated',
        message: 'Your profile picture has been updated successfully.',
      })
    } catch (err: any) {
      setPicError(err?.message || 'Upload failed. Please try again.')
      setTimeout(() => setPicError(null), 5000)
    } finally {
      setPicUploading(false)
    }
  }

  const fullName = user ? `${user.firstname} ${user.lastname}` : 'Guest'
  const email = user?.email ?? ''
  const initials = user ? `${user.firstname.charAt(0)}${user.lastname.charAt(0)}`.toUpperCase() : '?'
  const avatarUrl = user?.profilePicUrl ? resolveImageUrl(user.profilePicUrl) : null

  const now = new Date()
  const myEvents = registeredEvents.filter(e => new Date(e.endDate) >= now && e.status !== 'CANCELLED' && e.status !== 'COMPLETED')
  const pastEvents = registeredEvents.filter(e => new Date(e.endDate) < now || e.status === 'CANCELLED' || e.status === 'COMPLETED')

  const currentList = activeTab === 'own' ? ownEvents : activeTab === 'my' ? myEvents : pastEvents
  const emptyMessage = activeTab === 'own' ? "You haven't created any events yet." : activeTab === 'my' ? 'No upcoming registered events.' : 'No past events.'

  const createdAt = user?.createdAt
    ? new Date(user.createdAt).toLocaleDateString('en-US', { month: 'long', year: 'numeric' })
    : ''

  const modalConfig = {
    cancel: {
      icon: <svg width="48" height="48" viewBox="0 0 48 48" fill="none"><circle cx="24" cy="24" r="24" fill="#fff3cd"/><path d="M24 16v10" stroke="#d97706" strokeWidth="2.5" strokeLinecap="round"/><circle cx="24" cy="32" r="1.5" fill="#d97706"/></svg>,
      title: 'Cancel Event',
      message: (name: string) => <>Are you sure you want to cancel <strong>{name}</strong>? Participants will be notified. You can continue this event later.</>,
      confirmClass: 'btn-warning',
      confirmText: 'Cancel Event',
    },
    delete: {
      icon: <svg width="48" height="48" viewBox="0 0 48 48" fill="none"><circle cx="24" cy="24" r="24" fill="#f8d7da"/><path d="M17 20h14M20 20v10m4-10v10m-8-13l1-3h6l1 3M19 20l1 13h8l1-13" stroke="#dc3545" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/></svg>,
      title: 'Delete Event',
      message: (name: string) => <>Are you sure you want to permanently delete <strong>{name}</strong>? This action cannot be undone.</>,
      confirmClass: 'btn-danger',
      confirmText: 'Delete Permanently',
    },
    continue: {
      icon: <svg width="48" height="48" viewBox="0 0 48 48" fill="none"><circle cx="24" cy="24" r="24" fill="#d4edda"/><path d="M20 18l10 6-10 6V18z" stroke="#198754" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" fill="#198754"/></svg>,
      title: 'Continue Event',
      message: (name: string) => <>Resume <strong>{name}</strong> and set its status back to Upcoming?</>,
      confirmClass: 'btn-success',
      confirmText: 'Continue Event',
    },
  }

  return (
    <div className="min-vh-100 bg-light">
      <Navbar user={user} />

      <div className="text-center py-4">
        <h4 className="fw-bold mb-0">Profile</h4>
      </div>

      <div className="container pb-5" style={{ maxWidth: '1100px' }}>
        <div className="row g-4">

          {/* ══════ Profile Card ══════ */}
          <div className="col-12 col-md-4 col-lg-3">
            <div className="card shadow-sm border-0 overflow-hidden">
              <div style={{ height: 72, background: 'linear-gradient(135deg, #1e293b 0%, #334155 100%)' }} />
              <div className="card-body text-center" style={{ marginTop: -36 }}>

                {/* Hidden file input */}
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/*"
                  style={{ display: 'none' }}
                  onChange={handleFileChange}
                />

                {/* Clickable avatar */}
                <div
                  className="position-relative mx-auto"
                  style={{ width: 64, height: 64, cursor: picUploading ? 'wait' : 'pointer' }}
                  onClick={handleAvatarClick}
                  title="Click to change profile picture"
                >
                  {avatarUrl ? (
                    <img
                      src={avatarUrl}
                      alt="Profile"
                      className="rounded-circle shadow"
                      style={{
                        width: 64,
                        height: 64,
                        objectFit: 'cover',
                        border: '3px solid #fff',
                      }}
                    />
                  ) : (
                    <div
                      className="d-flex align-items-center justify-content-center rounded-circle fw-bold text-white shadow"
                      style={{
                        width: 64,
                        height: 64,
                        fontSize: 22,
                        background: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
                        border: '3px solid #fff',
                      }}
                    >
                      {initials}
                    </div>
                  )}

                  {/* Camera overlay */}
                  <div
                    className="position-absolute d-flex align-items-center justify-content-center rounded-circle shadow"
                    style={{
                      bottom: 0,
                      right: 0,
                      width: 22,
                      height: 22,
                      backgroundColor: '#1e293b',
                      border: '2px solid #fff',
                    }}
                  >
                    <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M23 19a2 2 0 01-2 2H3a2 2 0 01-2-2V8a2 2 0 012-2h4l2-3h6l2 3h4a2 2 0 012 2z"/>
                      <circle cx="12" cy="13" r="4"/>
                    </svg>
                  </div>

                  {/* Loading overlay */}
                  {picUploading && (
                    <div
                      className="position-absolute top-0 start-0 d-flex align-items-center justify-content-center rounded-circle"
                      style={{
                        width: 64,
                        height: 64,
                        backgroundColor: 'rgba(0,0,0,0.5)',
                      }}
                    >
                      <div className="spinner-border spinner-border-sm text-white" role="status" />
                    </div>
                  )}
                </div>

                {picError && (
                  <div className="alert alert-danger py-1 px-2 small mt-2 mb-0" style={{ fontSize: 11 }}>
                    {picError}
                  </div>
                )}

                <h6 className="fw-bold mt-2 mb-0">{fullName}</h6>
                <p className="text-muted mb-2" style={{ fontSize: 12 }}>Member since {createdAt}</p>

                <div className="d-flex justify-content-center gap-3 mb-3">
                  {isOrganizer && (
                    <div className="text-center">
                      <div className="fw-bold" style={{ fontSize: 18 }}>{ownEvents.length}</div>
                      <div className="text-muted" style={{ fontSize: 10 }}>Created</div>
                    </div>
                  )}
                  <div className="text-center">
                    <div className="fw-bold" style={{ fontSize: 18 }}>{myEvents.length}</div>
                    <div className="text-muted" style={{ fontSize: 10 }}>Joined</div>
                  </div>
                  <div className="text-center">
                    <div className="fw-bold" style={{ fontSize: 18 }}>{pastEvents.length}</div>
                    <div className="text-muted" style={{ fontSize: 10 }}>Past</div>
                  </div>
                </div>

                <hr className="my-2" />

                <div className="text-start px-2">
                  <p className="small mb-2 d-flex align-items-center gap-2 text-muted">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/>
                    </svg>
                    <span style={{ fontSize: 12, wordBreak: 'break-all' }}>{email}</span>
                  </p>
                  <p className="small mb-0 d-flex align-items-center gap-2 text-muted">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/>
                    </svg>
                    <span style={{ fontSize: 12 }}>{capitalize(user?.role ?? 'participant')}</span>
                  </p>
                </div>

                <button className="btn btn-sm btn-outline-dark w-100 mt-3" style={{ fontSize: 12 }} onClick={() => navigate('/settings')}>
                  Edit Profile
                </button>
              </div>
            </div>
          </div>

          {/* ══════ Events ══════ */}
          <div className="col-12 col-md-8 col-lg-9">
            <div className="d-flex mb-3 border rounded overflow-hidden bg-white" style={{ width: 'fit-content' }}>
              {isOrganizer && (
                <button className={`btn btn-sm px-4 py-2 rounded-0 border-0 ${activeTab === 'own' ? 'bg-dark text-white' : 'bg-white text-dark'}`} onClick={() => setActiveTab('own')}>
                  Own Events ({ownEvents.length})
                </button>
              )}
              <button className={`btn btn-sm px-4 py-2 rounded-0 border-0 ${activeTab === 'my' ? 'bg-dark text-white' : 'bg-white text-dark'}`} onClick={() => setActiveTab('my')}>
                My Events ({myEvents.length})
              </button>
              <button className={`btn btn-sm px-4 py-2 rounded-0 border-0 ${activeTab === 'past' ? 'bg-dark text-white' : 'bg-white text-dark'}`} onClick={() => setActiveTab('past')}>
                Past Events ({pastEvents.length})
              </button>
            </div>

            {loading ? (
              <div className="text-center py-5"><div className="spinner-border text-secondary" role="status" /></div>
            ) : currentList.length === 0 ? (
              <div className="text-center py-5"><p className="text-muted small">{emptyMessage}</p></div>
            ) : (
              <div className="d-flex flex-column gap-3">
                {currentList.map(event => {
                  const canEdit = event.status === 'UPCOMING' || event.status === 'ONGOING'
                  const canCancel = event.status === 'UPCOMING' || event.status === 'ONGOING'
                  const canContinue = event.status === 'CANCELLED'
                  const canDelete = event.status === 'CANCELLED' || event.status === 'COMPLETED'

                  return (
                    <div key={event.id} className="card shadow-sm border-0 p-0 overflow-hidden">
                      <div className="d-flex">
                        <img src={event.imageUrl ? resolveImageUrl(event.imageUrl) : '/placeholder.jpg'} alt={event.title} className="object-fit-cover flex-shrink-0" style={{ width: 140, height: 120 }} />
                        <div className="p-3 flex-grow-1 position-relative">
                          <div className="position-absolute top-0 end-0 m-2 d-flex gap-1">
                            <span className={`badge ${statusBadgeColor(event.status)}`} style={{ fontSize: '0.65rem' }}>{event.status}</span>
                            <span className="badge" style={{ ...catColor(event.category), fontSize: '0.65rem' }}>{event.category}</span>
                          </div>

                          <h6 className="fw-bold mb-1 pe-5">{event.title}</h6>
                          <p className="text-muted small mb-2" style={{ fontSize: 12 }}>{event.description}</p>

                          <div className="d-flex gap-3 flex-wrap align-items-center">
                            <span className="text-muted d-flex align-items-center gap-1" style={{ fontSize: 12 }}>
                              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                              {new Date(event.startDate).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })}
                            </span>
                            <span className="text-muted d-flex align-items-center gap-1" style={{ fontSize: 12 }}>
                              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
                              {event.location}
                            </span>
                            {event.participantCount != null && (
                              <span className="text-muted d-flex align-items-center gap-1" style={{ fontSize: 12 }}>
                                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>
                                {event.participantCount}/{event.maxParticipants ?? '∞'}
                              </span>
                            )}

                            {activeTab === 'own' && (
                              <div className="ms-auto d-flex gap-1">
                                {canEdit && (
                                  <button className="btn btn-outline-dark btn-sm py-0 px-2" style={{ fontSize: 11 }} onClick={() => setEditingEvent(event)}>Edit</button>
                                )}
                                {canCancel && (
                                  <button className="btn btn-outline-warning btn-sm py-0 px-2" style={{ fontSize: 11 }} onClick={() => setModal({ type: 'cancel', event })}>Cancel</button>
                                )}
                                {canContinue && (
                                  <button className="btn btn-outline-success btn-sm py-0 px-2" style={{ fontSize: 11 }} onClick={() => setModal({ type: 'continue', event })}>Continue</button>
                                )}
                                {canDelete && (
                                  <button className="btn btn-outline-danger btn-sm py-0 px-2" style={{ fontSize: 11 }} onClick={() => setModal({ type: 'delete', event })}>Delete</button>
                                )}
                              </div>
                            )}
                          </div>
                        </div>
                      </div>
                    </div>
                  )
                })}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* ── Action Modal ── */}
      {modal && (() => {
        const cfg = modalConfig[modal.type]
        return (
          <>
            <div className="modal-backdrop fade show" style={{ zIndex: 1040 }} onClick={() => !actionLoading && setModal(null)} />
            <div className="modal fade show d-block" style={{ zIndex: 1050 }} role="dialog">
              <div className="modal-dialog modal-dialog-centered modal-sm">
                <div className="modal-content border-0 shadow text-center p-4">
                  <div className="mx-auto mb-3">{cfg.icon}</div>
                  <h5 className="fw-bold mb-2">{cfg.title}</h5>
                  <p className="text-muted small mb-4">{cfg.message(modal.event.title)}</p>
                  <div className="d-flex gap-2">
                    <button className="btn btn-sm btn-outline-secondary flex-fill" disabled={actionLoading} onClick={() => setModal(null)}>Go Back</button>
                    <button className={`btn btn-sm ${cfg.confirmClass} flex-fill`} disabled={actionLoading} onClick={handleAction}>
                      {actionLoading ? <span className="spinner-border spinner-border-sm" /> : cfg.confirmText}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </>
        )
      })()}

      {/* ── Profile Pic Success Modal ── */}
      {successModal && (
        <>
          <div className="modal-backdrop fade show" style={{ zIndex: 1040 }} onClick={() => setSuccessModal(null)} />
          <div className="modal fade show d-block" style={{ zIndex: 1050 }} role="dialog">
            <div className="modal-dialog modal-dialog-centered modal-sm">
              <div className="modal-content border-0 shadow text-center p-4">
                <div className="mx-auto mb-3" style={{ width: 64, height: 64, borderRadius: '50%', background: '#d4edda', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#198754" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
                    <polyline points="20 6 9 17 4 12"/>
                  </svg>
                </div>
                <h5 className="fw-bold mb-2">{successModal.title}</h5>
                <p className="text-muted small mb-3">{successModal.message}</p>
                <button className="btn btn-success btn-sm w-100" onClick={() => setSuccessModal(null)}>
                  Done
                </button>
              </div>
            </div>
          </div>
        </>
      )}

      {/* ── Edit Event Modal ── */}
      <EditEventModal
        event={editingEvent}
        onClose={() => setEditingEvent(null)}
        onSuccess={() => { setEditingEvent(null); fetchData() }}
      />
    </div>
  )
}