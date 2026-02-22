import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { eventsApi, type EventDto } from '../services/api'

export default function Events() {
  const [events, setEvents] = useState<EventDto[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    eventsApi.list().then((r) => {
      setEvents(r.content || [])
      setLoading(false)
    }).catch(() => setLoading(false))
  }, [])

  if (loading) return <div>Loading...</div>
  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Upcoming Events</h1>
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {events.map((e) => (
          <Link key={e.id} to={`/events/${e.id}`} className="block p-4 bg-slate-800 rounded-lg border border-slate-700 hover:border-cyan-500">
            <h2 className="font-semibold text-lg">{e.title}</h2>
            <p className="text-slate-400 text-sm mt-1">{new Date(e.eventDate).toLocaleString()}</p>
            <p className="text-cyan-400 mt-2">${Number(e.price).toFixed(2)}</p>
          </Link>
        ))}
      </div>
      {events.length === 0 && <p className="text-slate-400">No events found.</p>}
    </div>
  )
}
