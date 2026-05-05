import { useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import { eventsApi } from './eventsApi'

const CATEGORIES = [
  'Music',
  'Sports',
  'Tech',
  'Arts',
  'Food & Drink',
  'Business',
  'Health',
]

interface CreateEventModalProps {
  show: boolean
  onClose: () => void
  onSuccess: () => void
}

interface EventForm {
  title: string
  description: string
  startDate: string
  endDate: string
  location: string
  category: string
  maxParticipants: string
}

export default function CreateEventModal({ show, onClose, onSuccess }: CreateEventModalProps) {
  const [form, setForm] = useState<EventForm>({
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
  const [error, setError] = useState<string>('')
  const [loading, setLoading] = useState<boolean>(false)

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
      await eventsApi.createWithImage({
        title: form.title,
        description: form.description,
        startDate: form.startDate,
        endDate: form.endDate,
        location: form.location,
        category: form.category,
        maxParticipants: Number(form.maxParticipants),
      }, image ?? undefined)

      handleClose()
      onSuccess()
    } catch (err: any) {
      setError(err.message || 'Unable to connect to server. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const handleClose = () => {
    setForm({
      title: '',
      description: '',
      startDate: '',
      endDate: '',
      location: '',
      category: 'Music',
      maxParticipants: '',
    })
    setImage(null)
    setImagePreview(null)
    setError('')
    onClose()
  }

  if (!show) return null

  return (
    <>
      {/* Backdrop */}
      <div
        className="modal-backdrop fade show"
        style={{ zIndex: 1040 }}
        onClick={handleClose}
      />

      {/* Modal */}
      <div
        className="modal fade show d-block"
        style={{ zIndex: 1050 }}
        role="dialog"
      >
        <div className="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
          <div className="modal-content border-0 shadow">

            {/* Header */}
            <div className="modal-header border-0 pb-0">
              <h5 className="modal-title fw-bold">Create Event</h5>
              <button
                type="button"
                className="btn-close"
                onClick={handleClose}
              />
            </div>

            {/* Body */}
            <div className="modal-body pt-2">
              <form id="createEventForm" onSubmit={handleSubmit}>

                {/* Title */}
                <div className="mb-3">
                  <label className="form-label small fw-medium">Event Title</label>
                  <input
                    name="title"
                    type="text"
                    className="form-control form-control-sm"
                    placeholder="Enter event title"
                    value={form.title}
                    onChange={handleChange}
                    required
                  />
                </div>

                {/* Description */}
                <div className="mb-3">
                  <label className="form-label small fw-medium">Description</label>
                  <textarea
                    name="description"
                    className="form-control form-control-sm"
                    placeholder="Describe your event"
                    rows={3}
                    value={form.description}
                    onChange={handleChange}
                    required
                  />
                </div>

                {/* Start & End Date */}
                <div className="row g-2 mb-3">
                  <div className="col">
                    <label className="form-label small fw-medium">Start Date & Time</label>
                    <input
                      name="startDate"
                      type="datetime-local"
                      className="form-control form-control-sm"
                      value={form.startDate}
                      onChange={handleChange}
                      required
                    />
                  </div>
                  <div className="col">
                    <label className="form-label small fw-medium">End Date & Time</label>
                    <input
                      name="endDate"
                      type="datetime-local"
                      className="form-control form-control-sm"
                      value={form.endDate}
                      onChange={handleChange}
                      required
                    />
                  </div>
                </div>

                {/* Location */}
                <div className="mb-3">
                  <label className="form-label small fw-medium">Location</label>
                  <input
                    name="location"
                    type="text"
                    className="form-control form-control-sm"
                    placeholder="Enter event location"
                    value={form.location}
                    onChange={handleChange}
                    required
                  />
                </div>

                {/* Category & Max Participants */}
                <div className="row g-2 mb-3">
                  <div className="col">
                    <label className="form-label small fw-medium">Category</label>
                    <select
                      name="category"
                      className="form-select form-select-sm"
                      value={form.category}
                      onChange={handleChange}
                      required
                    >
                      {CATEGORIES.map((cat) => (
                        <option key={cat} value={cat}>{cat}</option>
                      ))}
                    </select>
                  </div>
                  <div className="col">
                    <label className="form-label small fw-medium">Max Participants</label>
                    <input
                      name="maxParticipants"
                      type="number"
                      min="1"
                      className="form-control form-control-sm"
                      placeholder="e.g. 100"
                      value={form.maxParticipants}
                      onChange={handleChange}
                      required
                    />
                  </div>
                </div>

                {/* Image Upload */}
                <div className="mb-3">
                  <label className="form-label small fw-medium">Event Image</label>
                  <input
                    type="file"
                    className="form-control form-control-sm"
                    accept="image/*"
                    onChange={handleImageChange}
                  />
                  {imagePreview && (
                    <img
                      src={imagePreview}
                      alt="Preview"
                      className="mt-2 rounded"
                      style={{ width: '100%', maxHeight: '160px', objectFit: 'cover' }}
                    />
                  )}
                </div>

                {error && (
                  <div className="alert alert-danger py-2 small" role="alert">
                    {error}
                  </div>
                )}
              </form>
            </div>

            {/* Footer */}
            <div className="modal-footer border-0 pt-0">
              <button
                type="button"
                className="btn btn-sm btn-outline-danger"
                onClick={handleClose}
              >
                Cancel
              </button>
              <button
                type="submit"
                form="createEventForm"
                className="btn btn-sm btn-success"
                disabled={loading}
              >
                {loading ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                    Creating...
                  </>
                ) : 'Create Event'}
              </button>
            </div>

          </div>
        </div>
      </div>
    </>
  )
}