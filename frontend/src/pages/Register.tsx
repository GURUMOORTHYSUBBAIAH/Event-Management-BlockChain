import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Register() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [error, setError] = useState('')
  const { register } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    try {
      await register(email, password, displayName || undefined)
      navigate('/')
    } catch (err: unknown) {
      setError((err as Error).message)
    }
  }

  return (
    <div className="max-w-md mx-auto mt-16 p-6 bg-slate-800 rounded-xl border border-slate-700">
      <h1 className="text-2xl font-bold mb-6">Register</h1>
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <p className="text-red-400 text-sm">{error}</p>}
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          className="w-full px-4 py-2 rounded bg-slate-700 border border-slate-600 focus:border-cyan-500 outline-none"
          required
        />
        <input
          type="text"
          placeholder="Display Name (optional)"
          value={displayName}
          onChange={(e) => setDisplayName(e.target.value)}
          className="w-full px-4 py-2 rounded bg-slate-700 border border-slate-600 focus:border-cyan-500 outline-none"
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="w-full px-4 py-2 rounded bg-slate-700 border border-slate-600 focus:border-cyan-500 outline-none"
          minLength={6}
          required
        />
        <button type="submit" className="w-full py-2 bg-cyan-600 hover:bg-cyan-500 rounded font-medium">
          Register
        </button>
      </form>
      <p className="mt-4 text-slate-400">
        Already have an account? <Link to="/login" className="text-cyan-400">Login</Link>
      </p>
    </div>
  )
}
