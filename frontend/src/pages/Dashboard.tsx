import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { Client } from '@stomp/stompjs'
import { eventsApi, type EventAnalyticsDto } from '../services/api'

export default function Dashboard() {
  const { eventId } = useParams()
  const [analytics, setAnalytics] = useState<EventAnalyticsDto | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!eventId) return
    eventsApi.analytics(Number(eventId)).then(setAnalytics).finally(() => setLoading(false))
  }, [eventId])

  useEffect(() => {
    if (!eventId) return
    const client = new Client({
      brokerURL: (window.location.protocol === 'https:' ? 'wss:' : 'ws:') + window.location.host + '/ws',
      reconnectDelay: 5000,
    })
    client.onConnect = () => {
      client.subscribe('/topic/event/' + eventId + '/analytics', (msg) => {
        try {
          setAnalytics(JSON.parse(msg.body))
        } catch {}
      })
      client.subscribe('/topic/event/' + eventId + '/checkin', () => {
        eventsApi.analytics(Number(eventId)).then(setAnalytics)
      })
    }
    client.activate()
    return () => client.deactivate()
  }, [eventId])

  if (loading || !analytics) return <div>Loading...</div>
  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Dashboard: {analytics.eventTitle}</h1>
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatCard title="Total Applicants" value={analytics.totalApplicants} />
        <StatCard title="Selected" value={analytics.selectedCount} />
        <StatCard title="Paid" value={analytics.paidCount} />
        <StatCard title="NFTs Minted" value={analytics.nftsMinted} />
        <StatCard title="Checked In" value={analytics.checkedInCount} />
        <StatCard title="Certificates" value={analytics.certificatesIssued} />
        <StatCard title="Revenue" value={'$' + Number(analytics.revenue).toFixed(2)} />
        <StatCard title="Payment %" value={Number(analytics.paymentPercentage).toFixed(1) + '%'} />
      </div>
    </div>
  )
}

function StatCard({ title, value }: { title: string; value: string | number }) {
  return (
    <div className="p-4 bg-slate-800 rounded-lg border border-slate-700">
      <p className="text-slate-400 text-sm">{title}</p>
      <p className="text-xl font-bold mt-1">{value}</p>
    </div>
  )
}
