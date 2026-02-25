const API = 'http://localhost:8080/api'

function getAuthHeader(): HeadersInit {
  const token = localStorage.getItem('accessToken')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export async function fetchApi<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(API + path, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...getAuthHeader(), ...init?.headers },
  })

  if (!res.ok) {
    const error = await res.json().catch(() => {})
    throw new Error(error.message || 'Request failed')
  }

  return res.json()
}

export async function fetchBlob(path: string, init?: RequestInit): Promise<Blob> {
  const res = await fetch(API + path, {
    ...init,
    headers: { ...getAuthHeader(), ...init?.headers },
  })

  if (!res.ok) {
    const text = await res.text().catch(() => '')
    let message = 'Request failed'
    try {
      const json = text ? JSON.parse(text) : null
      if (json?.message) message = json.message
    } catch {
      // ignore
    }
    throw new Error(message)
  }

  return res.blob()
}

export const eventsApi = {
  list: (status = 'OPEN') => fetchApi<{ content: EventDto[] }>(`/events?status=${status}`),
  get: (id: number) => fetchApi<EventDto>(`/events/${id}`),
  create: (data: CreateEventRequest) =>
    fetchApi<EventDto>('/events', { method: 'POST', body: JSON.stringify(data) }),
  update: (id: number, data: CreateEventRequest) =>
    fetchApi<EventDto>(`/events/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  publish: (id: number) => fetchApi<EventDto>(`/events/${id}/publish`, { method: 'POST' }),
  analytics: (id: number) => fetchApi<EventAnalyticsDto>(`/events/${id}/analytics`),
}

export const applicationsApi = {
  apply: (eventId: number) =>
    fetchApi<ApplicationDto>(`/applications/events/${eventId}/apply`, { method: 'POST' }),
  byEvent: (eventId: number) => fetchApi<ApplicationDto[]>(`/applications/events/${eventId}`),
  my: () => fetchApi<ApplicationDto[]>('/applications/me'),
}

export const lotteryApi = {
  trigger: (eventId: number) =>
    fetchApi<void>(`/events/${eventId}/lottery/trigger`, { method: 'POST' }),
}

export const paymentsApi = {
  checkout: (applicationId: number) =>
    fetchApi<{ sessionId: string; url: string }>(`/payments/checkout/${applicationId}`, { method: 'POST' }),
  success: (sessionId: string) =>
    fetchApi<void>(`/payments/success?session_id=${sessionId}`, { method: 'POST' }),
}

export const ticketsApi = {
  my: () => fetchApi<TicketDto[]>('/tickets/me'),
  byEvent: (eventId: number) => fetchApi<TicketDto[]>(`/tickets/event/${eventId}`),
}

export const checkInApi = {
  checkIn: (eventId: number, tokenId: number) =>
    fetchApi<TicketDto>(`/checkin/events/${eventId}/tickets/${tokenId}`, { method: 'POST' }),
}

export const certificatesApi = {
  download: (ticketId: number) =>
    fetchBlob(`/certificates/ticket/${ticketId}/download`),
  verify: (certificateId: string) =>
    fetchApi<boolean>(`/certificates/verify/${certificateId}`),
}

export interface EventDto {
  id: number
  title: string
  description?: string
  category?: string
  eventDate: string
  location?: string
  price: number
  maxSeats: number
  lotteryDeadline: string
  status: string
}

export interface CreateEventRequest {
  title: string
  description?: string
  category?: string
  eventDate: string
  location?: string
  price: number
  maxSeats: number
  lotteryDeadline: string
}

export interface ApplicationDto {
  id: number
  userId: number
  eventId: number
  status: string
  userEmail?: string
  userDisplayName?: string
}

export interface TicketDto {
  id: number
  eventId: number
  tokenId: number
  checkedIn: boolean
  checkedInAt?: string
}

export interface EventAnalyticsDto {
  eventId: number
  eventTitle: string
  status: string
  totalApplicants: number
  selectedCount: number
  waitlistedCount: number
  paidCount: number
  nftsMinted: number
  checkedInCount: number
  certificatesIssued: number
  revenue: number
  paymentPercentage: number
  noShowRate: number
}
