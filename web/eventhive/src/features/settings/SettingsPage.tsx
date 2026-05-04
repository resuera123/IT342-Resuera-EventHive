import { useState, type ChangeEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { getUser, saveUser, clearUser } from '../../shared/utils/auth.ts'
import Navbar from '../../shared/components/Navbar.tsx'

export default function SettingsPage() {
  const navigate = useNavigate()
  const user = getUser()

  const [activeTab, setActiveTab] = useState<'profile' | 'notifications' | 'security'>('profile')

  // ── Profile state ──
  const [firstName, setFirstName] = useState(user?.firstname ?? '')
  const [lastName, setLastName] = useState(user?.lastname ?? '')
  const [email, setEmail] = useState(user?.email ?? '')
  const [profileMsg, setProfileMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null)
  const [profileLoading, setProfileLoading] = useState(false)

  // ── Security state ──
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [securityMsg, setSecurityMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null)
  const [securityLoading, setSecurityLoading] = useState(false)

  // ── Notification state ──
  const stored = JSON.parse(localStorage.getItem('eventhive_notif_prefs') ?? '{}')
  const [notifRegistration, setNotifRegistration] = useState(stored.registration ?? true)
  const [notifEventUpdates, setNotifEventUpdates] = useState(stored.eventUpdates ?? true)
  const [notifCancellation, setNotifCancellation] = useState(stored.cancellation ?? true)
  const [notifNewParticipant, setNotifNewParticipant] = useState(stored.newParticipant ?? true)
  const [notifMsg, setNotifMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null)

  const handleSaveProfile = async (e: React.FormEvent) => {
    e.preventDefault()
    setProfileMsg(null)
    setProfileLoading(true)

    try {
      const res = await fetch('http://localhost:8081/api/users/profile', {
        method: 'PUT',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ firstname: firstName, lastname: lastName, email }),
      })
      const data = await res.json()

      if (res.ok) {
        saveUser({ id: data.id, firstname: data.firstname, lastname: data.lastname, email: data.email, role: data.role, createdAt: data.createdAt })
        setProfileMsg({ type: 'success', text: 'Profile updated successfully.' })
      } else {
        setProfileMsg({ type: 'error', text: data.message || 'Failed to update profile.' })
      }
    } catch {
      setProfileMsg({ type: 'error', text: 'Unable to connect to server.' })
    } finally {
      setProfileLoading(false)
    }
  }

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault()
    setSecurityMsg(null)

    if (newPassword !== confirmPassword) {
      setSecurityMsg({ type: 'error', text: 'New passwords do not match.' })
      return
    }
    if (newPassword.length < 8) {
      setSecurityMsg({ type: 'error', text: 'Password must be at least 8 characters.' })
      return
    }

    setSecurityLoading(true)
    try {
      const res = await fetch('http://localhost:8081/api/users/password', {
        method: 'PUT',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ currentPassword, newPassword }),
      })
      const data = await res.json()

      if (res.ok) {
        setSecurityMsg({ type: 'success', text: 'Password changed successfully.' })
        setCurrentPassword(''); setNewPassword(''); setConfirmPassword('')
      } else {
        setSecurityMsg({ type: 'error', text: data.message || 'Failed to change password.' })
      }
    } catch {
      setSecurityMsg({ type: 'error', text: 'Unable to connect to server.' })
    } finally {
      setSecurityLoading(false)
    }
  }

  const handleSaveNotifications = () => {
    localStorage.setItem('eventhive_notif_prefs', JSON.stringify({
      registration: notifRegistration,
      eventUpdates: notifEventUpdates,
      cancellation: notifCancellation,
      newParticipant: notifNewParticipant,
    }))
    setNotifMsg({ type: 'success', text: 'Notification preferences saved.' })
    setTimeout(() => setNotifMsg(null), 3000)
  }

  return (
    <div className="min-vh-100 bg-light">
      <Navbar user={user} />

      <div className="container py-5" style={{ maxWidth: '900px' }}>
        <div className="card shadow-sm rounded-4 border-0">
          <div className="card-body p-4">
            <div className="mb-4">
              <h2 className="fw-bold mb-1">Settings</h2>
              <p className="text-muted mb-0">Manage your account preferences</p>
            </div>

            {/* Tabs */}
            <div className="d-flex gap-2 mb-4 rounded-4 border bg-white p-2">
              {(['profile', 'notifications', 'security'] as const).map(tab => (
                <button
                  key={tab}
                  type="button"
                  className={`btn btn-sm rounded-pill flex-fill border-0 ${activeTab === tab ? 'btn-dark text-white' : 'btn-light text-muted'}`}
                  onClick={() => setActiveTab(tab)}
                >
                  {tab === 'profile' && 'Profile'}
                  {tab === 'notifications' && 'Notifications'}
                  {tab === 'security' && 'Security'}
                </button>
              ))}
            </div>

            {/* ═══ Profile ═══ */}
            {activeTab === 'profile' && (
              <form onSubmit={handleSaveProfile} className="bg-white rounded-4 border p-4">
                <h5 className="fw-semibold mb-1">Profile Information</h5>
                <p className="text-muted small mb-4">Update your personal information</p>

                <div className="row g-3 mb-3">
                  <div className="col-12 col-md-6">
                    <label htmlFor="firstName" className="form-label small fw-semibold">First Name</label>
                    <input id="firstName" type="text" className="form-control form-control-sm" value={firstName} onChange={(e: ChangeEvent<HTMLInputElement>) => setFirstName(e.target.value)} required />
                  </div>
                  <div className="col-12 col-md-6">
                    <label htmlFor="lastName" className="form-label small fw-semibold">Last Name</label>
                    <input id="lastName" type="text" className="form-control form-control-sm" value={lastName} onChange={(e: ChangeEvent<HTMLInputElement>) => setLastName(e.target.value)} required />
                  </div>
                </div>

                <div className="mb-4">
                  <label htmlFor="emailAddress" className="form-label small fw-semibold">Email Address</label>
                  <input id="emailAddress" type="email" className="form-control form-control-sm" value={email} onChange={(e: ChangeEvent<HTMLInputElement>) => setEmail(e.target.value)} required />
                </div>

                {profileMsg && (
                  <div className={`alert py-2 small ${profileMsg.type === 'success' ? 'alert-success' : 'alert-danger'}`}>{profileMsg.text}</div>
                )}

                <button type="submit" className="btn btn-dark btn-sm" disabled={profileLoading}>
                  {profileLoading ? <><span className="spinner-border spinner-border-sm me-2" />Saving...</> : 'Save Changes'}
                </button>
              </form>
            )}

            {/* ═══ Notifications ═══ */}
            {activeTab === 'notifications' && (
              <div className="bg-white rounded-4 border p-4">
                <h5 className="fw-semibold mb-1">In-App Notifications</h5>
                <p className="text-muted small mb-4">Choose which notifications appear in your bell icon</p>

                <div className="d-flex flex-column gap-3">
                  <div className="d-flex justify-content-between align-items-center">
                    <div>
                      <div className="fw-medium small">Registration Confirmations</div>
                      <div className="text-muted" style={{ fontSize: 12 }}>Get notified when you successfully register for an event</div>
                    </div>
                    <div className="form-check form-switch mb-0">
                      <input className="form-check-input" type="checkbox" role="switch" checked={notifRegistration} onChange={() => setNotifRegistration(!notifRegistration)} />
                    </div>
                  </div>

                  <hr className="my-0" />

                  <div className="d-flex justify-content-between align-items-center">
                    <div>
                      <div className="fw-medium small">Event Updates</div>
                      <div className="text-muted" style={{ fontSize: 12 }}>Get notified when an event you joined is resumed or updated</div>
                    </div>
                    <div className="form-check form-switch mb-0">
                      <input className="form-check-input" type="checkbox" role="switch" checked={notifEventUpdates} onChange={() => setNotifEventUpdates(!notifEventUpdates)} />
                    </div>
                  </div>

                  <hr className="my-0" />

                  <div className="d-flex justify-content-between align-items-center">
                    <div>
                      <div className="fw-medium small">Cancellation Alerts</div>
                      <div className="text-muted" style={{ fontSize: 12 }}>Get notified when an event you joined is cancelled</div>
                    </div>
                    <div className="form-check form-switch mb-0">
                      <input className="form-check-input" type="checkbox" role="switch" checked={notifCancellation} onChange={() => setNotifCancellation(!notifCancellation)} />
                    </div>
                  </div>

                  <hr className="my-0" />

                  <div className="d-flex justify-content-between align-items-center">
                    <div>
                      <div className="fw-medium small">New Participants</div>
                      <div className="text-muted" style={{ fontSize: 12 }}>Get notified when someone registers for your event (organizers)</div>
                    </div>
                    <div className="form-check form-switch mb-0">
                      <input className="form-check-input" type="checkbox" role="switch" checked={notifNewParticipant} onChange={() => setNotifNewParticipant(!notifNewParticipant)} />
                    </div>
                  </div>
                </div>

                {notifMsg && (
                  <div className={`alert py-2 small mt-3 ${notifMsg.type === 'success' ? 'alert-success' : 'alert-danger'}`}>{notifMsg.text}</div>
                )}

                <button className="btn btn-dark btn-sm mt-4" onClick={handleSaveNotifications}>
                  Save Preferences
                </button>
              </div>
            )}

            {/* ═══ Security ═══ */}
            {activeTab === 'security' && (
              <div className="d-flex flex-column gap-4">
                <form onSubmit={handleChangePassword} className="bg-white rounded-4 border p-4">
                  <h5 className="fw-semibold mb-1">Change Password</h5>
                  <p className="text-muted small mb-4">Update your password to keep your account secure</p>

                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Current Password</label>
                    <input type="password" className="form-control form-control-sm" value={currentPassword} onChange={e => setCurrentPassword(e.target.value)} required />
                  </div>
                  <div className="mb-3">
                    <label className="form-label small fw-semibold">New Password</label>
                    <input type="password" className="form-control form-control-sm" value={newPassword} onChange={e => setNewPassword(e.target.value)} required />
                    <div className="form-text" style={{ fontSize: 11 }}>Must be at least 8 characters</div>
                  </div>
                  <div className="mb-4">
                    <label className="form-label small fw-semibold">Confirm New Password</label>
                    <input type="password" className="form-control form-control-sm" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} required />
                  </div>

                  {securityMsg && (
                    <div className={`alert py-2 small ${securityMsg.type === 'success' ? 'alert-success' : 'alert-danger'}`}>{securityMsg.text}</div>
                  )}

                  <button type="submit" className="btn btn-dark btn-sm" disabled={securityLoading}>
                    {securityLoading ? <><span className="spinner-border spinner-border-sm me-2" />Updating...</> : 'Update Password'}
                  </button>
                </form>

                <div className="bg-white rounded-4 border border-danger p-4">
                  <h5 className="fw-semibold text-danger mb-1">Danger Zone</h5>
                  <p className="text-muted small mb-3">Irreversible actions on your account</p>
                  <div className="d-flex justify-content-between align-items-center">
                    <div>
                      <div className="fw-medium small">Delete Account</div>
                      <div className="text-muted" style={{ fontSize: 12 }}>Permanently delete your account and all associated data</div>
                    </div>
                    <button className="btn btn-outline-danger btn-sm" onClick={() => alert('Account deletion is not yet available.')}>
                      Delete Account
                    </button>
                  </div>
                </div>
              </div>
            )}

          </div>
        </div>
      </div>
    </div>
  )
}