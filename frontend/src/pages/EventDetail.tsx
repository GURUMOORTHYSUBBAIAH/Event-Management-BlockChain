import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { eventsApi, applicationsApi, lotteryApi, paymentsApi, type EventDto } from '../services/api'

export default function EventDetail() {
  const { id } = useParams()
  const { token, user, roles } = useAuth()
  const [event, setEvent] = useState<EventDto | null>(null)
  const [applications, setApplications] = useState<any[]>([])
  const [myApp, setMyApp] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [action, setAction] = useState('')

  const isAdmin = roles.some((r) => ['SUPER_ADMIN', 'ORG_ADMIN', 'EVENT_HEAD', 'TEAM_MEMBER'].includes(r))
  const canTriggerLottery = roles.some((r) => ['SUPER_ADMIN', 'ORG_ADMIN', 'EVENT_HEAD'].includes(r))

  useEffect(() => {
    if (!id) return
    eventsApi.get(Number(id)).then(setEvent).finally(() => setLoading(false))
    applicationsApi.byEvent(Number(id)).then(setApplications)
    if (token) applicationsApi.my().then((list) => setMyApp(list.find((a) => a.eventId === Number(id))))
  }, [id, token])

  const handleApply = async () => {
    setAction('apply')
    try {
      await applicationsApi.apply(Number(id))
      const list = await applicationsApi.my()
      setMyApp(list.find((a) => a.eventId === Number(id)))
    } finally {
      setAction('')
    }
  }

  const handleTriggerLottery = async () => {
    setAction('lottery')
    try {
      await lotteryApi.trigger(Number(id))
      await Promise.all([
        eventsApi.get(Number(id)).then(setEvent),
        applicationsApi.byEvent(Number(id)).then(setApplications),
      ])
    } finally {
      setAction('')
    }
  }

  const handlePay = async () => {
    if (!myApp) return
    setAction('pay')
    try {
      const { url } = await paymentsApi.checkout(myApp.id)
      window.location.href = url
    } finally {
      setAction('')
    }
  }

  if (loading || !event) return <div>Loading...</div>

  const deadlinePassed = new Date(event.lotteryDeadline) < new Date()
  const selected = myApp?.status === 'SELECTED'
  const paid = myApp?.status === 'PAID'

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">{event.title}</h1>
      <p className="text-slate-400 mb-4">{event.description}</p>
      <div className="flex gap-4 text-sm mb-6">
        <span>📅 {new Date(event.eventDate).toLocaleString('en-IN', {
          day: '2-digit',
          month: 'short',
          year: 'numeric',
          hour: '2-digit',
          minute: '2-digit',
        })}</span>
        <span>📍 {event.location}</span>
        <span>💰 ₹{Number(event.price).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
        <span>🎫 {event.maxSeats} seats</span>
        <span className="text-cyan-400">{event.status}</span>
      </div>

      {token && !myApp && event.status === 'OPEN' && (
        <button onClick={handleApply} disabled={!!action} className="px-4 py-2 bg-cyan-600 rounded">
          {action === 'apply' ? 'Applying...' : 'Apply'}
        </button>
      )}

      {token && selected && !paid && (
        <button onClick={handlePay} disabled={!!action} className="px-4 py-2 bg-green-600 rounded ml-2">
          {action === 'pay' ? 'Redirecting...' : 'Pay with Stripe'}
        </button>
      )}

      {token && paid && (
        <Link to="/my-tickets" className="inline-block px-4 py-2 bg-slate-600 rounded">
          View My Ticket
        </Link>
      )}

      {canTriggerLottery && event.status === 'OPEN' && deadlinePassed && (
        <button onClick={handleTriggerLottery} disabled={!!action} className="ml-4 px-4 py-2 bg-amber-600 rounded">
          {action === 'lottery' ? 'Running...' : 'Trigger Lottery'}
        </button>
      )}

      {isAdmin && (
        <div className="mt-8">
          <Link to={`/dashboard/${id}`} className="text-cyan-400">View Dashboard</Link>
        </div>
      )}

      {isAdmin && applications.length > 0 && (
        <div className="mt-8">
          <h2 className="text-lg font-semibold mb-2">Applications ({applications.length})</h2>
          <ul className="space-y-1 text-sm">
            {applications.slice(0, 20).map((a) => (
              <li key={a.id} className="flex justify-between">
                <span>{a.userDisplayName || a.userEmail}</span>
                <span className="text-cyan-400">{a.status}</span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}
