import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { eventsApi, type EventDto } from '../services/api'

export default function Events() {
  const [events, setEvents] = useState<EventDto[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        setLoading(true)
        const response = await eventsApi.list()
        setEvents(response.content || [])
      } catch (error) {
        console.error('Failed to fetch events:', error)
      } finally {
        setLoading(false)
      }
    }

    fetchEvents()
  }, [])

  if (loading) return <div className="flex justify-center items-center h-64">
    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 border-t-transparent"></div>
  </div>

  return (
    <div className="space-y-4">
      <div className="flex flex-col md:flex-row md:items-end md:justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold">Upcoming Events</h1>
          <p className="text-slate-400 text-sm">
            Discover curated experiences across India. Prices shown in <span className="font-semibold">INR (₹)</span>.
          </p>
        </div>
      </div>
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {events.map((e) => (
          <Link
            key={e.id}
            to={`/events/${e.id}`}
            className="block p-4 bg-slate-800 rounded-xl border border-slate-700 hover:border-cyan-500 transition-colors shadow-sm"
          >
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
            <p className="text-emerald-400 mt-2">
              ₹{Number(e.price).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
            </p>
          </Link>
        ))}
      </div>
      {events.length === 0 && <p className="text-slate-400">No events found.</p>}
    </div>
  )
}
