import { useState } from 'react'
import type { ChangeEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { clearUser } from '../utils/auth'

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

export default function DashboardPage() {
  const navigate = useNavigate()
  const [search, setSearch] = useState<string>('')
  const [category, setCategory] = useState<string>('All Categories')

  const handleLogout = () => {
    clearUser()
    navigate('/login')
  }

  return (
    <div className="min-vh-100 bg-light">

      {/* Navbar */}
      <nav className="navbar navbar-expand-lg navbar-light bg-white border-bottom shadow-sm px-4">
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
            <button className="btn btn-sm btn-outline-secondary active">Events</button>

            {/* Profile Icon */}
            <button className="btn btn-sm btn-light d-flex align-items-center justify-content-center p-2" title="Profile" onClick={() => navigate('/profile')}>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="8" r="4"/>
                <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/>
              </svg>
            </button>

            {/* Logout Icon */}
            <button
              className="btn btn-sm btn-light d-flex align-items-center justify-content-center p-2"
              title="Logout"
              onClick={handleLogout}
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/>
                <polyline points="16 17 21 12 16 7"/>
                <line x1="21" y1="12" x2="9" y2="12"/>
              </svg>
            </button>
          </div>
        </div>
      </nav>

      {/* Hero */}
      <div className="container-fluid px-4 pt-4 pb-2">
        <h2 className="fw-bold mb-1">Discover Events</h2>
        <p className="text-muted small">Find and join events that match your interests</p>
      </div>

      {/* Search & Filter Toolbar */}
      <div className="container-fluid px-4 pb-4">
        <div className="d-flex gap-2 align-items-center">
          {/* Search */}
          <div className="input-group input-group-sm flex-grow-1" style={{ minWidth: 0 }}>
            <span className="input-group-text bg-white border-end-0">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="11" cy="11" r="8"/>
                <line x1="21" y1="21" x2="16.65" y2="16.65"/>
              </svg>
            </span>
            <input
              type="text"
              className="form-control border-start-0"
              style={{ minWidth: 0 }}
              placeholder="Search events..."
              value={search}
              onChange={(e: ChangeEvent<HTMLInputElement>) => setSearch(e.target.value)}
            />
          </div>

          {/* Category Dropdown */}
          <div className="dropdown">
            <button
              className="btn btn-sm btn-outline-secondary dropdown-toggle"
              type="button"
              data-bs-toggle="dropdown"
              aria-expanded="false"
            >
              {category}
            </button>
            <ul className="dropdown-menu dropdown-menu-end">
              {CATEGORIES.map((cat) => (
                <li key={cat}>
                  <button
                    className={`dropdown-item small ${category === cat ? 'active' : ''}`}
                    onClick={() => setCategory(cat)}
                  >
                    {cat}
                  </button>
                </li>
              ))}
            </ul>
          </div>
        </div>
      </div>

      {/* Empty State */}
      <div className="container-fluid px-4">
        <div className="d-flex flex-column align-items-center justify-content-center py-5 text-center">
          <div
            className="rounded-circle bg-light border d-flex align-items-center justify-content-center mb-3"
            style={{ width: '72px', height: '72px', color: '#adb5bd' }}
          >
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.3" strokeLinecap="round" strokeLinejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
              <line x1="16" y1="2" x2="16" y2="6"/>
              <line x1="8" y1="2" x2="8" y2="6"/>
              <line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
          </div>
          <p className="fw-semibold text-secondary mb-1">No events found.</p>
          <p className="text-muted small">Check back later or try a different category.</p>
        </div>
      </div>

    </div>
  )
}