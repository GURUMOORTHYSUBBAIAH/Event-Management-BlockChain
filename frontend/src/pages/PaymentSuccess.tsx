import { useEffect } from 'react'
import { useSearchParams, Link } from 'react-router-dom'
import { paymentsApi } from '../services/api'

export default function PaymentSuccess() {
  const [params] = useSearchParams()
  const sessionId = params.get('session_id')

  useEffect(() => {
    if (sessionId) paymentsApi.success(sessionId).catch(console.error)
  }, [sessionId])

  return (
    <div className="max-w-md mx-auto mt-16 text-center">
      <h1 className="text-2xl font-bold text-green-400">Payment Successful!</h1>
      <p className="mt-4 text-slate-400">Your NFT ticket will be minted shortly.</p>
      <Link to="/my-tickets" className="mt-6 inline-block px-6 py-2 bg-cyan-600 rounded">View My Tickets</Link>
    </div>
  )
}
