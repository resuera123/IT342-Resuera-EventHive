import { useState, useEffect, useCallback } from 'react'
import type { ChangeEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { getUser, saveUser } from '../../shared/utils/auth.ts'
import type { UserAuth } from '../../shared/utils/auth.ts'
import { API_BASE_URL } from '../../shared/api/client.ts'
import Navbar from '../../shared/components/Navbar.tsx'
import CreateEventModal from './CreateEventModal.tsx'
import EventDetailModal from './EventDetailModal.tsx'
import { eventsApi } from './eventsApi.ts'
import { authApi } from '../auth/authApi.ts'

const CATEGORIES: string[] = [
  'All Categories',
  'Music',
  'Sports',
  'Tech',
  'Arts',
  'Food & Drink',
  'Business',
  'Health',
]

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

const statusColor = (s: string) => {
  switch (s) {
    case 'UPCOMING': return 'bg-primary'
    case 'ONGOING': return 'bg-success'
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
  }
  return map[c] ?? { backgroundColor: '#6c757d', color: '#fff' }
}

export default function DashboardPage() {
  const navigate = useNavigate()
  const [search, setSearch] = useState<string>('')
  const [category, setCategory] = useState<string>('All Categories')
  const [user, setUser] = useState<UserAuth | null>(getUser())
  const [showCreateModal, setShowCreateModal] = useState<boolean>(false)
  const [events, setEvents] = useState<EventItem[]>([])
  const [selectedEvent, setSelectedEvent] = useState<EventItem | null>(null)

  const fetchEvents = useCallback(() => {
    eventsApi.getAll()
      .then(data => setEvents(data as EventItem[]))
      .catch(err => console.error("Failed to load events", err))
  }, [])

  useEffect(() => { fetchEvents() }, [fetchEvents])

  useEffect(() => {
    if (user) return
    authApi.getCurrentUser()
      .then(data => {
        if (data.id !== null) {
          const u: UserAuth = { id: data.id, firstname: data.firstname, lastname: data.lastname, email: data.email, role: data.role, createdAt: data.createdAt }
          saveUser(u); setUser(u)
        } else { navigate('/login') }
      })
      .catch(() => navigate('/login'))
  }, [])

  const filteredEvents = events.filter(e => {
    if (e.status === 'COMPLETED' || e.status === 'CANCELLED') return false
    const s = search.toLowerCase()
    const matchSearch = !s || e.title.toLowerCase().includes(s) || e.description.toLowerCase().includes(s)
    const matchCat = category === 'All Categories' || e.category === category
    return matchSearch && matchCat
  })

  return (
    <div className="min-vh-100 bg-light">
      <Navbar user={user} />

      <div className="container-fluid px-4 pt-4 pb-2 d-flex justify-content-between align-items-center">
        <div>
          <h2 className="fw-bold mb-1">Discover Events</h2>
          <p className="text-muted small mb-0">Find and join events that match your interests</p>
        </div>
        {user?.role?.toLowerCase() === 'organizer' && (
          <button className="btn btn-sm btn-success d-flex align-items-center gap-1" onClick={() => setShowCreateModal(true)}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            Create Event
          </button>
        )}
      </div>

      <div className="container-fluid px-4 pb-4">
        <div className="d-flex gap-2 align-items-center">
          <div className="input-group input-group-sm flex-grow-1" style={{ minWidth: 0 }}>
            <span className="input-group-text bg-white border-end-0">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
              </svg>
            </span>
            <input type="text" className="form-control border-start-0" style={{ minWidth: 0 }} placeholder="Search events..." value={search} onChange={(e: ChangeEvent<HTMLInputElement>) => setSearch(e.target.value)} />
          </div>
          <div className="dropdown">
            <button className="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown">{category}</button>
            <ul className="dropdown-menu dropdown-menu-end">
              {CATEGORIES.map(cat => (
                <li key={cat}><button className={`dropdown-item small ${category === cat ? 'active' : ''}`} onClick={() => setCategory(cat)}>{cat}</button></li>
              ))}
            </ul>
          </div>
        </div>
      </div>

      <div className="container-fluid px-4">
        {filteredEvents.length === 0 ? (
          <div className="d-flex flex-column align-items-center justify-content-center py-5 text-center">
            <p className="fw-semibold text-secondary mb-1">No events found.</p>
            <p className="text-muted small">Check back later or try a different category.</p>
          </div>
        ) : (
          <div className="row g-3 pb-4">
            {filteredEvents.map(event => (
              <div key={event.id} className="col-lg-4 col-md-6">
                <div className="card shadow-sm h-100" style={{ cursor: 'pointer' }} onClick={() => setSelectedEvent(event)}>
                  <div className="row g-0">
                    <div className="col-4">
                      <img src={event.imageUrl ? `${API_BASE_URL}${event.imageUrl}` : "/placeholder.jpg"} className="img-fluid rounded-start h-100" style={{ objectFit: "cover" }} />
                    </div>
                    <div className="col-8">
                      <div className="card-body p-2">
                        <div className="d-flex justify-content-between align-items-start mb-1">
                          <h6 className="fw-bold mb-0 pe-1" style={{ fontSize: '0.85rem' }}>{event.title}</h6>
                          <div className="d-flex gap-1 flex-shrink-0">
                            <span className={`badge ${statusColor(event.status)}`} style={{ fontSize: '0.65rem' }}>{event.status}</span>
                            <span className="badge" style={{ ...catColor(event.category), fontSize: '0.65rem' }}>{event.category}</span>
                          </div>
                        </div>
                        <p className="small text-muted mb-1">
                          {new Date(event.startDate).toLocaleDateString()} • {new Date(event.startDate).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}
                        </p>
                        <p className="small text-muted mb-1">
                          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16"><path d="M8 16s6-5.686 6-10A6 6 0 0 0 2 6c0 4.314 6 10 6 10m0-7a3 3 0 1 1 0-6 3 3 0 0 1 0 6"/></svg> {event.location}
                        </p>
                        <p className="small text-muted mb-0">
                          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16"><path d="M7 14s-1 0-1-1 1-4 5-4 5 3 5 4-1 1-1 1zm4-6a3 3 0 1 0 0-6 3 3 0 0 0 0 6m-5.784 6A2.24 2.24 0 0 1 5 13c0-1.355.68-2.75 1.936-3.72A6.3 6.3 0 0 0 5 9c-4 0-5 3-5 4s1 1 1 1zM4.5 8a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5"/></svg> {event.participantCount}/{event.maxParticipants} participants
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <CreateEventModal show={showCreateModal} onClose={() => setShowCreateModal(false)} onSuccess={() => { setShowCreateModal(false); fetchEvents() }} />
      <EventDetailModal event={selectedEvent} user={user} onClose={() => setSelectedEvent(null)} onRegistered={fetchEvents} />
    </div>
  )
}