import { useState, type ChangeEvent, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { getUser } from '../utils/auth'
import { clearUser } from '../utils/auth'

export default function SettingsPage() {
  const navigate = useNavigate()
  const user = getUser()
  const [firstName, setFirstName] = useState(user?.firstname ?? 'Alex')
  const [lastName, setLastName] = useState(user?.lastname ?? 'Johnson')
  const [email, setEmail] = useState(user?.email ?? 'alex.johnson@example.com')
  const [activeTab, setActiveTab] = useState<'profile' | 'notifications' | 'security'>('profile')

  const handleSave = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    // TODO: save settings to backend or local storage
  }

  const handleLogout = () => {
      clearUser()
      navigate('/login')
    }

  return (
    <div className="min-vh-100 bg-light">
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
            <button className="btn btn-sm btn-link text-dark text-decoration-none" onClick={() => navigate('/dashboard')}>
              Events
            </button>
            <button className="btn btn-sm btn-light d-flex align-items-center justify-content-center p-2" title="Profile" onClick={() => navigate('/profile')}>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="8" r="4"/>
                <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/>
              </svg>
            </button>
            <button className="btn btn-sm btn-light d-flex align-items-center justify-content-center p-2" title="Logout" onClick={handleLogout}>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/>
                <polyline points="16 17 21 12 16 7"/>
                <line x1="21" y1="12" x2="9" y2="12"/>
              </svg>
            </button>
          </div>
        </div>
      </nav>

      <div className="container py-5" style={{ maxWidth: '900px' }}>

        <div className="card shadow-sm rounded-4 border-0">
          <div className="card-body p-4">
            <div className="mb-4">
              <h2 className="fw-bold mb-1">Settings</h2>
              <p className="text-muted mb-0">Manage your account preferences</p>
            </div>

            <div className="d-flex gap-2 mb-4 rounded-4 border bg-white p-2">
              {['profile', 'notifications', 'security'].map((tab) => (
                <button
                  key={tab}
                  type="button"
                  className={`btn btn-sm rounded-pill flex-fill border-0 ${activeTab === tab ? 'btn-dark text-white' : 'btn-light text-muted'}`}
                  onClick={() => setActiveTab(tab as 'profile' | 'notifications' | 'security')}
                >
                  {tab === 'profile' && 'Profile'}
                  {tab === 'notifications' && 'Notifications'}
                  {tab === 'security' && 'Security'}
                </button>
              ))}
            </div>

            <form onSubmit={handleSave} className="bg-white rounded-4 border p-4">
              <h5 className="fw-semibold mb-3">Profile Information</h5>
              <p className="text-muted small mb-4">Update your personal information</p>

              <div className="row g-3 mb-3">
                <div className="col-12 col-md-6">
                  <label htmlFor="firstName" className="form-label small fw-semibold">
                    Firstname
                  </label>
                  <input
                    id="firstName"
                    type="text"
                    className="form-control form-control-sm"
                    value={firstName}
                    onChange={(e: ChangeEvent<HTMLInputElement>) => setFirstName(e.target.value)}
                  />
                </div>
                <div className="col-12 col-md-6">
                  <label htmlFor="lastName" className="form-label small fw-semibold">
                    Lastname
                  </label>
                  <input
                    id="lastName"
                    type="text"
                    className="form-control form-control-sm"
                    value={lastName}
                    onChange={(e: ChangeEvent<HTMLInputElement>) => setLastName(e.target.value)}
                  />
                </div>
              </div>

              <div className="mb-4">
                <label htmlFor="emailAddress" className="form-label small fw-semibold">
                  Email Address
                </label>
                <input
                  id="emailAddress"
                  type="email"
                  className="form-control form-control-sm"
                  value={email}
                  onChange={(e: ChangeEvent<HTMLInputElement>) => setEmail(e.target.value)}
                />
              </div>

              <button type="submit" className="btn btn-dark btn-sm">
                Save Changes
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}
