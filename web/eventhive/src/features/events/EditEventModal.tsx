import { useState, useEffect } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import { resolveImageUrl } from '../../shared/utils/images'
import { eventsApi } from './eventsApi'

const CATEGORIES = ['Music', 'Sports', 'Tech', 'Arts', 'Food & Drink', 'Business', 'Health']

interface EventItem {
  id: number
  title: string
  description: string
  startDate: string
  endDate: string
  location: string
  category: string
  imageUrl?: string
  maxParticipants?: number
}

interface EditEventModalProps {
  event: EventItem | null
  onClose: () => void
  onSuccess: () => void
}

export default function EditEventModal({ event, onClose, onSuccess }: EditEventModalProps) {
  const [form, setForm] = useState({
    title: '',
    description: '',
    startDate: '',
    endDate: '',
    location: '',
    category: 'Music',
    maxParticipants: '',
  })
  const [image, setImage] = useState<File | null>(null)
  const [imagePreview, setImagePreview] = useState<string | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (event) {
      setForm({
        title: event.title,
        description: event.description,
        startDate: event.startDate?.slice(0, 16) ?? '',
        endDate: event.endDate?.slice(0, 16) ?? '',
        location: event.location,
        category: event.category,
        maxParticipants: String(event.maxParticipants ?? ''),
      })
      setImagePreview(event.imageUrl ? resolveImageUrl(event.imageUrl) : null)
      setImage(null)
      setError('')
    }
  }, [event])

  if (!event) return null

  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value })
    setError('')
  }

  const handleImageChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      setImage(file)
      setImagePreview(URL.createObjectURL(file))
    }
  }

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setError('')

    if (new Date(form.startDate) >= new Date(form.endDate)) {
      setError('End date must be after start date')
      return
    }

    setLoading(true)

    try {
      await eventsApi.update(event.id, {
        title: form.title,
        description: form.description,
        startDate: form.startDate,
        endDate: form.endDate,
        location: form.location,
        category: form.category,
        maxParticipants: Number(form.maxParticipants),
      }, image ?? undefined)

      onSuccess()
      onClose()
    } catch (err: any) {
      setError(err.message || 'Unable to connect to server.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <div className="modal-backdrop fade show" style={{ zIndex: 1040 }} onClick={onClose} />
      <div className="modal fade show d-block" style={{ zIndex: 1050 }} role="dialog">
        <div className="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
          <div className="modal-content border-0 shadow">

            <div className="modal-header border-0 pb-0">
              <h5 className="modal-title fw-bold">Edit Event</h5>
              <button type="button" className="btn-close" onClick={onClose} />
            </div>

            <div className="modal-body pt-2">
              <form id="editEventForm" onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label className="form-label small fw-medium">Event Title</label>
                  <input name="title" type="text" className="form-control form-control-sm" value={form.title} onChange={handleChange} required />
                </div>

                <div className="mb-3">
                  <label className="form-label small fw-medium">Description</label>
                  <textarea name="description" className="form-control form-control-sm" rows={3} value={form.description} onChange={handleChange} required />
                </div>

                <div className="row g-2 mb-3">
                  <div className="col">
                    <label className="form-label small fw-medium">Start Date & Time</label>
                    <input name="startDate" type="datetime-local" className="form-control form-control-sm" value={form.startDate} onChange={handleChange} required />
                  </div>
                  <div className="col">
                    <label className="form-label small fw-medium">End Date & Time</label>
                    <input name="endDate" type="datetime-local" className="form-control form-control-sm" value={form.endDate} onChange={handleChange} required />
                  </div>
                </div>

                <div className="mb-3">
                  <label className="form-label small fw-medium">Location</label>
                  <input name="location" type="text" className="form-control form-control-sm" value={form.location} onChange={handleChange} required />
                </div>

                <div className="row g-2 mb-3">
                  <div className="col">
                    <label className="form-label small fw-medium">Category</label>
                    <select name="category" className="form-select form-select-sm" value={form.category} onChange={handleChange} required>
                      {CATEGORIES.map(cat => <option key={cat} value={cat}>{cat}</option>)}
                    </select>
                  </div>
                  <div className="col">
                    <label className="form-label small fw-medium">Max Participants</label>
                    <input name="maxParticipants" type="number" min="1" className="form-control form-control-sm" value={form.maxParticipants} onChange={handleChange} required />
                  </div>
                </div>

                <div className="mb-3">
                  <label className="form-label small fw-medium">Event Image</label>
                  <input type="file" className="form-control form-control-sm" accept="image/*" onChange={handleImageChange} />
                  {imagePreview && (
                    <img src={imagePreview} alt="Preview" className="mt-2 rounded" style={{ width: '100%', maxHeight: 160, objectFit: 'cover' }} />
                  )}
                </div>

                {error && <div className="alert alert-danger py-2 small">{error}</div>}
              </form>
            </div>

            <div className="modal-footer border-0 pt-0">
              <button type="button" className="btn btn-sm btn-outline-secondary" onClick={onClose}>Cancel</button>
              <button type="submit" form="editEventForm" className="btn btn-sm btn-dark" disabled={loading}>
                {loading ? <><span className="spinner-border spinner-border-sm me-2" />Saving...</> : 'Save Changes'}
              </button>
            </div>

          </div>
        </div>
      </div>
    </>
  )
}