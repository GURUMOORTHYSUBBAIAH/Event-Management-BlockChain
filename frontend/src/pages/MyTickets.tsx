import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { QRCodeSVG } from 'qrcode.react'
import { ticketsApi, eventsApi, certificatesApi, type TicketDto, type EventDto } from '../services/api'

export default function MyTickets() {
  const [tickets, setTickets] = useState<TicketDto[]>([])
  const [eventsMap, setEventsMap] = useState<Record<number, EventDto>>({})
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    ticketsApi.my().then((list) => {
      setTickets(list)
      const ids = [...new Set(list.map((t) => t.eventId))]
      Promise.all(ids.map((id) => eventsApi.get(id))).then((events) => {
        const map: Record<number, EventDto> = {}
        events.forEach((e) => { map[e.id] = e })
        setEventsMap(map)
      })
    }).finally(() => setLoading(false))
  }, [])

  const handleDownloadCert = async (ticketId: number) => {
    const res = await certificatesApi.download(ticketId)
    const blob = await res.blob()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'certificate.pdf'
    a.click()
  }

  if (loading) return <div>Loading...</div>
  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">My Tickets</h1>
      <div className="grid gap-6 md:grid-cols-2">
        {tickets.map((t) => {
          const event = eventsMap[t.eventId]
          const qrData = JSON.stringify({ eventId: t.eventId, tokenId: t.tokenId })
          return (
            <div key={t.id} className="p-6 bg-slate-800 rounded-lg border border-slate-700">
              <h2 className="font-semibold text-lg">{event?.title || `Event #${t.eventId}`}</h2>
              <p className="text-slate-400 text-sm mt-1">Token ID: {t.tokenId}</p>
              <p className="text-slate-400 text-sm">{t.checkedIn ? '✓ Checked in' : 'Not checked in'}</p>
              <div className="mt-4 flex justify-center p-4 bg-white rounded">
                <QRCodeSVG value={qrData} size={128} />
              </div>
              {t.checkedIn && (
                <button
                  onClick={() => handleDownloadCert(t.id)}
                  className="mt-4 px-4 py-2 bg-cyan-600 rounded"
                >
                  Download Certificate
                </button>
              )}
            </div>
          )
        })}
      </div>
      {tickets.length === 0 && <p className="text-slate-400">No tickets.</p>}
    </div>
  )
}
