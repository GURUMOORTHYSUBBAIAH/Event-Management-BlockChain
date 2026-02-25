import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { applicationsApi, eventsApi, paymentsApi, type ApplicationDto, type EventDto } from '../services/api'

export default function MyApplications() {
  const [apps, setApps] = useState<ApplicationDto[]>([])
  const [eventsMap, setEventsMap] = useState<Record<number, EventDto>>({})
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    applicationsApi.my().then((list) => {
      setApps(list)
      const ids = [...new Set(list.map((a) => a.eventId))]
      Promise.all(ids.map((id) => eventsApi.get(id))).then((events) => {
        const map: Record<number, EventDto> = {}
        events.forEach((e) => { map[e.id] = e })
        setEventsMap(map)
      })
    }).finally(() => setLoading(false))
  }, [])

  const handlePay = async (app: ApplicationDto) => {
    const { url } = await paymentsApi.checkout(app.id)
    window.location.href = url
  }

  if (loading) return <div>Loading...</div>
  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">My Applications</h1>
      <div className="space-y-4">
        {apps.map((app) => {
          const event = eventsMap[app.eventId]
          return (
            <div key={app.id} className="p-4 bg-slate-800 rounded-lg border border-slate-700 flex justify-between items-center">
              <div>
                <Link to={`/events/${app.eventId}`} className="font-semibold hover:text-cyan-400">
                  {event?.title || `Event #${app.eventId}`}
                </Link>
                <p className="text-slate-400 text-sm mt-1">
                  Status: <span className="font-semibold">{app.status}</span>
                </p>
                {event && (
                  <p className="text-emerald-400 text-xs mt-1">
                    Amount: ₹{Number(event.price).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                  </p>
                )}
              </div>
              {app.status === 'SELECTED' && (
                <button onClick={() => handlePay(app)} className="px-4 py-2 bg-green-600 rounded">
                  Pay
                </button>
              )}
            </div>
          )
        })}
      </div>
      {apps.length === 0 && <p className="text-slate-400">No applications.</p>}
    </div>
  )
}
