import { useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { saveUser } from '../utils/auth'

interface LoginForm {
  email: string
  password: string
}

interface AuthResponse {
  status: string
  id: number
  firstname: string
  lastname: string
  email: string
  role: string
  createdAt: string
}

export default function LoginPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState<LoginForm>({ email: '', password: '' })
  const [error, setError] = useState<string>('')
  const [loading, setLoading] = useState<boolean>(false)

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value })
    setError('')
  }

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setLoading(true)
    setError('')

    try {
      const res = await fetch('http://localhost:8081/api/auth/login', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: form.email, password: form.password }),
      })

      const data: AuthResponse = await res.json()

      if (data.status === 'Login successful') {
        saveUser({ id: data.id, firstname: data.firstname, lastname: data.lastname, email: data.email, role: data.role, createdAt: data.createdAt })
        navigate('/dashboard')
      } else {
        setError(data.status || 'Invalid credentials')
      }
    } catch (_err) {
      setError('Unable to connect to server. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const handleGoogleLogin = () => {
    window.location.href = 'http://localhost:8081/oauth2/authorization/google'
  }

  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center bg-light">
      <div className="card shadow-sm p-4" style={{ width: '100%', maxWidth: '380px' }}>

        {/* Logo */}
        <div className="text-center mb-3">
          <span
            className="d-inline-flex align-items-center justify-content-center bg-dark text-white fw-bold rounded"
            style={{ width: '44px', height: '44px', fontSize: '14px' }}
          >
            EH
          </span>
        </div>

        <h4 className="text-center fw-bold mb-1">EventHive</h4>
        <p className="text-center text-muted small mb-4">Login to discover amazing events</p>

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label htmlFor="email" className="form-label small fw-medium">Email</label>
            <input
              id="email"
              name="email"
              type="email"
              className="form-control form-control-sm"
              placeholder="you@example.com"
              value={form.email}
              onChange={handleChange}
              required
            />
          </div>

          <div className="mb-3">
            <label htmlFor="password" className="form-label small fw-medium">Password</label>
            <input
              id="password"
              name="password"
              type="password"
              className="form-control form-control-sm"
              placeholder="••••••••"
              value={form.password}
              onChange={handleChange}
              required
            />
          </div>

          {error && (
            <div className="alert alert-danger py-2 small text-center" role="alert">
              {error}
            </div>
          )}

          <div className="d-grid mb-3">
            <button type="submit" className="btn btn-dark btn-sm" disabled={loading}>
              {loading ? (
                <>
                  <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  Logging in...
                </>
              ) : 'Login'}
            </button>
          </div>
        </form>

        {/* Divider */}
        <div className="d-flex align-items-center gap-2 mb-3">
          <hr className="flex-grow-1 m-0" />
          <span className="text-muted small">or</span>
          <hr className="flex-grow-1 m-0" />
        </div>

        {/* Google Login */}
        <div className="d-grid mb-3">
          <button
            type="button"
            className="btn btn-outline-danger btn-sm d-flex align-items-center justify-content-center gap-2"
            onClick={handleGoogleLogin}
          >
            <svg width="16" height="16" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M17.64 9.205c0-.639-.057-1.252-.164-1.841H9v3.481h4.844a4.14 4.14 0 01-1.796 2.716v2.259h2.908c1.702-1.567 2.684-3.875 2.684-6.615z" fill="#4285F4"/>
              <path d="M9 18c2.43 0 4.467-.806 5.956-2.18l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332A8.997 8.997 0 009 18z" fill="#34A853"/>
              <path d="M3.964 10.71A5.41 5.41 0 013.682 9c0-.593.102-1.17.282-1.71V4.958H.957A8.996 8.996 0 000 9c0 1.452.348 2.827.957 4.042l3.007-2.332z" fill="#FBBC05"/>
              <path d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0A8.997 8.997 0 00.957 4.958L3.964 7.29C4.672 5.163 6.656 3.58 9 3.58z" fill="#EA4335"/>
            </svg>
            Login with Google
          </button>
        </div>

        <p className="text-center text-muted small mb-0">
          Don't have an account?{' '}
          <Link to="/register" className="text-dark fw-medium">Register here</Link>
        </p>
      </div>
    </div>
  )
}