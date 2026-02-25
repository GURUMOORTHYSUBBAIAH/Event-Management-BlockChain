import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { eventsApi, type EventDto, type CreateEventRequest } from '../services/api'

type Mode = 'list' | 'create' | 'edit'

const emptyForm: CreateEventRequest = {
  title: '',
  description: '',
  category: '',
  eventDate: '',
  location: '',
  price: 0,
  maxSeats: 0,
  lotteryDeadline: '',
}

export default function AdminEvents() {
  const { roles } = useAuth()
  const navigate = useNavigate()
  const [events, setEvents] = useState<EventDto[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [mode, setMode] = useState<Mode>('list')
  const [editingId, setEditingId] = useState<number | null>(null)
  const [form, setForm] = useState<CreateEventRequest>(emptyForm)

  const isAdmin = useMemo(
    () => roles.some((r) => ['SUPER_ADMIN', 'ORG_ADMIN', 'EVENT_HEAD'].includes(r)),
    [roles],
  )

  useEffect(() => {
    if (!isAdmin) {
      navigate('/')
      return
    }
    setLoading(true)
    eventsApi
      .list('DRAFT')
      .then((res) => setEvents(res.content || []))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [isAdmin, navigate])

  const startCreate = () => {
    setError(null)
    setForm({
      ...emptyForm,
      eventDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().slice(0, 16),
      lotteryDeadline: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000).toISOString().slice(0, 16),
    })
    setEditingId(null)
    setMode('create')
  }

  const startEdit = (e: EventDto) => {
    setError(null)
    setEditingId(e.id)
    setForm({
      title: e.title,
      description: e.description,
      category: e.category,
      eventDate: e.eventDate.slice(0, 16),
      location: e.location,
      price: e.price,
      maxSeats: e.maxSeats,
      lotteryDeadline: e.lotteryDeadline.slice(0, 16),
    })
    setMode('edit')
  }

  const handleChange = (field: keyof CreateEventRequest, value: string | number) => {
    setForm((f) => ({ ...f, [field]: value }))
  }

  const reloadDrafts = async () => {
    const res = await eventsApi.list('DRAFT')
    setEvents(res.content || [])
  }

  const handleSave = async (publishAfter = false) => {
    try {
      setSaving(true)
      setError(null)
      const payload: CreateEventRequest = {
        ...form,
        price: Number(form.price) || 0,
        maxSeats: Number(form.maxSeats) || 0,
        eventDate: new Date(form.eventDate).toISOString(),
        lotteryDeadline: new Date(form.lotteryDeadline).toISOString(),
      }
      let saved: EventDto
      if (mode === 'edit' && editingId) {
        saved = await eventsApi.update(editingId, payload)
      } else {
        saved = await eventsApi.create(payload)
      }
      if (publishAfter) {
        await eventsApi.publish(saved.id)
      }
      await reloadDrafts()
      setMode('list')
    } catch (e: unknown) {
      setError((e as Error).message)
    } finally {
      setSaving(false)
    }
  }

  if (!isAdmin) {
    return null
  }

  return (
    <div className="space-y-6">
      <header className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Admin – Manage Events</h1>
          <p className="text-slate-400 text-sm">
            Create, edit and publish events. All amounts are in <span className="font-semibold">INR (₹)</span>.
          </p>
        </div>
        {mode === 'list' && (
          <button
            onClick={startCreate}
            className="px-4 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-sm font-semibold"
          >
            + New Event
          </button>
        )}
      </header>

      {error && <div className="px-4 py-2 bg-red-900/40 border border-red-600 rounded text-sm">{error}</div>}

      {mode === 'list' && (
        <>
          {loading ? (
            <div className="flex justify-center items-center h-40">
              <div className="h-10 w-10 border-2 border-cyan-400 border-t-transparent rounded-full animate-spin" />
            </div>
          ) : (
            <div className="grid gap-4 md:grid-cols-2">
              {events.map((e) => (
                <div
                  key={e.id}
                  className="p-4 rounded-xl bg-slate-800 border border-slate-700 hover:border-cyan-500 transition-colors"
                >
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <h2 className="font-semibold text-lg">{e.title}</h2>
                      <p className="text-slate-400 text-sm mt-1">
                        {new Date(e.eventDate).toLocaleString('en-IN', {
                          day: '2-digit',
                          month: 'short',
                          year: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </p>
                      <p className="text-emerald-400 text-sm mt-1">
                        ₹{Number(e.price).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                      </p>
                      <p className="text-slate-400 text-xs mt-1">
                        Seats: {e.maxSeats} · Status: <span className="font-semibold">{e.status}</span>
                      </p>
                    </div>
                    <div className="flex flex-col gap-1">
                      {e.status === 'DRAFT' && (
                        <button
                          onClick={() => eventsApi.publish(e.id).then(reloadDrafts)}
                          className="px-3 py-1 rounded bg-cyan-600 hover:bg-cyan-500 text-xs font-semibold"
                        >
                          Publish
                        </button>
                      )}
                      <button
                        onClick={() => startEdit(e)}
                        className="px-3 py-1 rounded bg-slate-700 hover:bg-slate-600 text-xs font-semibold"
                      >
                        Edit
                      </button>
                    </div>
                  </div>
                </div>
              ))}
              {events.length === 0 && (
                <p className="text-slate-400 col-span-full text-sm">
                  No draft events. Click &quot;New Event&quot; to create one.
                </p>
              )}
            </div>
          )}
        </>
      )}

      {mode !== 'list' && (
        <div className="max-w-xl">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="font-semibold text-lg">
              {mode === 'create' ? 'Create Event' : 'Edit Event'}{' '}
              <span className="text-xs text-slate-400">(times are local)</span>
            </h2>
            <button
              onClick={() => setMode('list')}
              className="text-sm text-slate-300 hover:text-cyan-400 underline-offset-4 hover:underline"
            >
              Back to list
            </button>
          </div>
          <div className="space-y-4 p-4 bg-slate-800 rounded-xl border border-slate-700 shadow-lg">
            <div>
              <label className="block text-sm text-slate-300 mb-1">Title</label>
              <input
                className="w-full px-3 py-2 rounded bg-slate-900 border border-slate-600 focus:border-cyan-500 outline-none"
                value={form.title}
                onChange={(e) => handleChange('title', e.target.value)}
                required
              />
            </div>
            <div>
              <label className="block text-sm text-slate-300 mb-1">Description</label>
              <textarea
                className="w-full px-3 py-2 rounded bg-slate-900 border border-slate-600 focus:border-cyan-500 outline-none"
                rows={3}
                value={form.description || ''}
                onChange={(e) => handleChange('description', e.target.value)}
              />
            </div>
            <div className="grid md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm text-slate-300 mb-1">Category</label>
                <input
                  className="w-full px-3 py-2 rounded bg-slate-900 border border-slate-600 focus:border-cyan-500 outline-none"
                  value={form.category || ''}
                  onChange={(e) => handleChange('category', e.target.value)}
                />
              </div>
              <div>
                <label className="block text-sm text-slate-300 mb-1">Location</label>
                <input
                  className="w-full px-3 py-2 rounded bg-slate-900 border border-slate-600 focus:border-cyan-500 outline-none"
                  value={form.location || ''}
                  onChange={(e) => handleChange('location', e.target.value)}
                />
              </div>
            </div>
            <div className="grid md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm text-slate-300 mb-1">Event Date &amp; Time</label>
                <input
                  type="datetime-local"
                  className="w-full px-3 py-2 rounded bg-slate-900 border border-slate-600 focus:border-cyan-500 outline-none"
                  value={form.eventDate}
                  onChange={(e) => handleChange('eventDate', e.target.value)}
                />
              </div>
              <div>
                <label className="block text-sm text-slate-300 mb-1">Lottery Deadline</label>
                <input
                  type="datetime-local"
                  className="w-full px-3 py-2 rounded bg-slate-900 border border-slate-600 focus:border-cyan-500 outline-none"
                  value={form.lotteryDeadline}
                  onChange={(e) => handleChange('lotteryDeadline', e.target.value)}
                />
              </div>
            </div>
            <div className="grid md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm text-slate-300 mb-1">Price (₹)</label>
                <input
                  type="number"
                  min={0}
                  step="0.01"
                  className="w-full px-3 py-2 rounded bg-slate-900 border border-slate-600 focus:border-cyan-500 outline-none"
                  value={form.price}
                  onChange={(e) => handleChange('price', e.target.valueAsNumber)}
                />
              </div>
              <div>
                <label className="block text-sm text-slate-300 mb-1">Max Seats</label>
                <input
                  type="number"
                  min={1}
                  className="w-full px-3 py-2 rounded bg-slate-900 border border-slate-600 focus:border-cyan-500 outline-none"
                  value={form.maxSeats}
                  onChange={(e) => handleChange('maxSeats', e.target.valueAsNumber)}
                />
              </div>
            </div>
            <div className="flex gap-3 pt-2">
              <button
                disabled={saving}
                onClick={() => handleSave(false)}
                className="px-4 py-2 rounded-lg bg-slate-700 hover:bg-slate-600 text-sm font-semibold disabled:opacity-60"
              >
                {saving ? 'Saving...' : 'Save as Draft'}
              </button>
              <button
                disabled={saving}
                onClick={() => handleSave(true)}
                className="px-4 py-2 rounded-lg bg-cyan-600 hover:bg-cyan-500 text-sm font-semibold disabled:opacity-60"
              >
                {saving ? 'Saving...' : 'Save & Publish'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

