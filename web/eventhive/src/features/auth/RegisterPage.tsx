import { useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { authApi } from './authApi'

interface RegisterForm {
  firstname: string
  lastname: string
  email: string
  password: string
  confirmPassword: string
}

export default function RegisterPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState<RegisterForm>({
    firstname: '',
    lastname: '',
    email: '',
    password: '',
    confirmPassword: '',
  })
  const [error, setError] = useState<string>('')
  const [loading, setLoading] = useState<boolean>(false)

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value })
    setError('')
  }

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setError('')

    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match')
      return
    }

    if (form.password.length < 6) {
      setError('Password must be at least 6 characters')
      return
    }

    setLoading(true)

    try {
      const data = await authApi.register({
        firstname: form.firstname,
        lastname: form.lastname,
        email: form.email,
        password: form.password,
      })

      if (data.status === 'User registered successfully') {
        navigate('/login')
      } else {
        setError(data.status || 'Registration failed. Please try again.')
      }
    } catch (_err) {
      setError('Unable to connect to server. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center bg-light">
      <div className="card shadow-sm p-4" style={{ width: '100%', maxWidth: '420px' }}>

        {/* Logo */}
        <div className="text-center mb-3">
          <span
            className="d-inline-flex align-items-center justify-content-center bg-dark text-white fw-bold rounded"
            style={{ width: '44px', height: '44px', fontSize: '14px' }}
          >
            EH
          </span>
        </div>

        <h4 className="text-center fw-bold mb-1">Join EventHive</h4>
        <p className="text-center text-muted small mb-4">Create your account to get started</p>

        <form onSubmit={handleSubmit}>
          {/* First & Last Name */}
          <div className="row g-2 mb-3">
            <div className="col">
              <label htmlFor="firstname" className="form-label small fw-medium">First Name</label>
              <input
                id="firstname"
                name="firstname"
                type="text"
                className="form-control form-control-sm"
                placeholder="Enter Firstname"
                value={form.firstname}
                onChange={handleChange}
                required
              />
            </div>
            <div className="col">
              <label htmlFor="lastname" className="form-label small fw-medium">Last Name</label>
              <input
                id="lastname"
                name="lastname"
                type="text"
                className="form-control form-control-sm"
                placeholder="Enter Lastname"
                value={form.lastname}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          {/* Email */}
          <div className="mb-3">
            <label htmlFor="email" className="form-label small fw-medium">Email</label>
            <input
              id="email"
              name="email"
              type="email"
              className="form-control form-control-sm"
              placeholder="Enter Email"
              value={form.email}
              onChange={handleChange}
              required
            />
          </div>

          {/* Password */}
          <div className="mb-3">
            <label htmlFor="password" className="form-label small fw-medium">Password</label>
            <input
              id="password"
              name="password"
              type="password"
              className="form-control form-control-sm"
              placeholder="Enter password"
              value={form.password}
              onChange={handleChange}
              required
            />
          </div>

          {/* Confirm Password */}
          <div className="mb-3">
            <label htmlFor="confirmPassword" className="form-label small fw-medium">Confirm Password</label>
            <input
              id="confirmPassword"
              name="confirmPassword"
              type="password"
              className="form-control form-control-sm"
              placeholder="Enter password again"
              value={form.confirmPassword}
              onChange={handleChange}
              required
            />
          </div>

          {error && (
            <div className="alert alert-danger py-2 small text-center" role="alert">
              {error}
            </div>
          )}

          <div className="d-grid">
            <button type="submit" className="btn btn-dark btn-sm" disabled={loading}>
              {loading ? (
                <>
                  <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  Registering...
                </>
              ) : 'Register'}
            </button>
          </div>
        </form>

        <p className="text-center text-muted small mt-3 mb-0">
          Already have an account?{' '}
          <Link to="/login" className="text-dark fw-medium">Login here</Link>
        </p>
      </div>
    </div>
  )
}