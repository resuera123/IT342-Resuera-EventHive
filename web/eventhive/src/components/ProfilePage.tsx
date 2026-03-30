import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getUser, clearUser } from '../utils/auth'

interface Event {
  id: number
  title: string
  description: string
  date: string
  location: string
  category: string
  image: string
}

const myEvents: Event[] = [
  {
    id: 1,
    title: 'Tech Innovation Summit 2026',
    description:
      'Join us for the largest tech innovation summit of the year. Featuring keynote speakers from leading tech companies, workshops, and networking opportunities.',
    date: 'March 15, 2026',
    location: 'San Francisco Convention Center',
    category: 'Technology',
    image: 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=200&h=120&fit=crop',
  },
  {
    id: 2,
    title: 'Summer Music Festival',
    description:
      'Experience an unforgettable day of live music featuring local and international artists across multiple stages.',
    date: 'June 26, 2026',
    location: 'Golden Gate Park',
    category: 'Music',
    image: 'https://images.unsplash.com/photo-1501281668745-f7f57925c3b4?w=200&h=120&fit=crop',
  },
  {
    id: 3,
    title: 'Digital Marketing Workshop',
    description:
      'Learn the latest digital marketing strategies from industry experts. Hands-on workshop covering SEO, social media, and content marketing.',
    date: 'March 25, 2026',
    location: 'Downtown Business Center',
    category: 'Workshop',
    image: 'https://images.unsplash.com/photo-1552664730-d307ca884978?w=200&h=120&fit=crop',
  },
]

const pastEvents: Event[] = [
  {
    id: 4,
    title: 'Web Dev Conference 2025',
    description:
      'A full-day conference covering the latest trends in web development, from frontend frameworks to cloud deployments.',
    date: 'November 10, 2025',
    location: 'Tech Hub Auditorium',
    category: 'Technology',
    image: 'https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=200&h=120&fit=crop',
  },
  {
    id: 5,
    title: 'Jazz Night Live',
    description:
      'An intimate evening of smooth jazz with talented local musicians in a cozy downtown venue.',
    date: 'October 5, 2025',
    location: 'Blue Note Lounge',
    category: 'Music',
    image: 'https://images.unsplash.com/photo-1415201364774-f6f0bb35f28f?w=200&h=120&fit=crop',
  },
]

const categoryColors: Record<string, string> = {
  Technology: 'bg-dark text-white',
  Music: 'bg-dark text-white',
  Workshop: 'bg-dark text-white',
  Arts: 'bg-dark text-white',
  Sports: 'bg-dark text-white',
}

export default function ProfilePage() {
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState<'my' | 'past'>('my')
  const user = getUser()

  const fullName = user ? `${user.firstname} ${user.lastname}` : 'Guest'
  const email = user?.email ?? ''

  const events = activeTab === 'my' ? myEvents : pastEvents

  const handleLogout = () => {
    clearUser()
    navigate('/login')
  }

  return (
    <div className="min-vh-100 bg-light">

      {/* Navbar */}
      <nav className="navbar navbar-light bg-white border-bottom shadow-sm px-4">
        <div className="container-fluid px-0">
          <a className="navbar-brand d-flex align-items-center gap-2 fw-bold" href="#">
            <span
              className="d-inline-flex align-items-center justify-content-center bg-dark text-white fw-bold rounded"
              style={{ width: '32px', height: '32px', fontSize: '11px' }}
            >
              EH
            </span>
            EventHive
          </a>

          <div className="d-flex align-items-center gap-2 ms-auto">
            <button className="btn btn-sm btn-link text-dark text-decoration-none" onClick={() => navigate('/dashboard')}>
              Events
            </button>

            {/* Settings Icon */}
            <button className="btn btn-sm btn-light d-flex align-items-center justify-content-center p-2" title="Settings" onClick={() => navigate('/settings')}>
              <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="12" r="3"/>
                <path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z"/>
              </svg>
            </button>

            {/* Logout Icon */}
            <button
              className="btn btn-sm btn-light d-flex align-items-center justify-content-center p-2"
              title="Logout"
              onClick={handleLogout}
            >
              <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/>
                <polyline points="16 17 21 12 16 7"/>
                <line x1="21" y1="12" x2="9" y2="12"/>
              </svg>
            </button>
          </div>
        </div>
      </nav>

      {/* Page Title */}
      <div className="text-center py-4">
        <h4 className="fw-bold mb-0">ProfilePage</h4>
      </div>

      {/* Main Content */}
      <div className="container pb-5" style={{ maxWidth: '1100px' }}>
        <div className="row g-4">

          {/* Left — Profile Card */}
          <div className="col-12 col-md-4 col-lg-3">
            <div className="card shadow-sm border-0 p-4 text-center">
              <h6 className="fw-bold mb-1">{fullName}</h6>
              <p className="text-muted small mb-3">Member since January 2026</p>

              <div className="text-start mb-2">
                <p className="small mb-1 d-flex align-items-center gap-2">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                    <polyline points="22,6 12,13 2,6"/>
                  </svg>
                  <span className="text-muted">{email}</span>
                </p>
                <p className="small mb-0 d-flex align-items-center gap-2">
                </p>
              </div>
            </div>
          </div>

          {/* Right — Events */}
          <div className="col-12 col-md-8 col-lg-9">

            {/* Tabs */}
            <div className="d-flex mb-3 border rounded overflow-hidden bg-white" style={{ width: 'fit-content' }}>
              <button
                className={`btn btn-sm px-4 py-2 rounded-0 border-0 ${activeTab === 'my' ? 'bg-dark text-white' : 'bg-white text-dark'}`}
                onClick={() => setActiveTab('my')}
              >
                My Events ({myEvents.length})
              </button>
              <button
                className={`btn btn-sm px-4 py-2 rounded-0 border-0 ${activeTab === 'past' ? 'bg-dark text-white' : 'bg-white text-dark'}`}
                onClick={() => setActiveTab('past')}
              >
                Past Events
              </button>
            </div>

            {/* Event Cards */}
            <div className="d-flex flex-column gap-3">
              {events.map((event) => (
                <div key={event.id} className="card shadow-sm border-0 p-0 overflow-hidden">
                  <div className="d-flex">
                    {/* Event Image */}
                    <img
                      src={event.image}
                      alt={event.title}
                      className="object-fit-cover flex-shrink-0"
                      style={{ width: '140px', height: '120px' }}
                    />

                    {/* Event Info */}
                    <div className="p-3 flex-grow-1 position-relative">
                      {/* Category Badge */}
                      <span className={`badge position-absolute top-0 end-0 m-2 ${categoryColors[event.category] ?? 'bg-secondary text-white'}`}>
                        {event.category}
                      </span>

                      <h6 className="fw-bold mb-1 pe-5">{event.title}</h6>
                      <p className="text-muted small mb-2" style={{ fontSize: '12px' }}>{event.description}</p>

                      <div className="d-flex gap-3">
                        <span className="text-muted d-flex align-items-center gap-1" style={{ fontSize: '12px' }}>
                          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                            <line x1="16" y1="2" x2="16" y2="6"/>
                            <line x1="8" y1="2" x2="8" y2="6"/>
                            <line x1="3" y1="10" x2="21" y2="10"/>
                          </svg>
                          {event.date}
                        </span>
                        <span className="text-muted d-flex align-items-center gap-1" style={{ fontSize: '12px' }}>
                          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/>
                            <circle cx="12" cy="10" r="3"/>
                          </svg>
                          {event.location}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>

          </div>
        </div>
      </div>
    </div>
  )
}