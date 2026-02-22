import { Outlet, Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Layout() {
  const { token, user, logout } = useAuth()
  const navigate = useNavigate()
  const handleLogout = () => {
    logout()
    navigate('/login')
  }
  return (
    <div className="min-h-screen bg-slate-900 text-white">
      <nav className="border-b border-slate-700 bg-slate-800/50">
        <div className="max-w-6xl mx-auto px-4 flex items-center justify-between h-14">
          <Link to="/" className="font-bold text-xl">EventChain</Link>
          <div className="flex gap-4">
            <Link to="/" className="hover:text-cyan-400">Events</Link>
            {token && (
              <>
                <Link to="/my-applications" className="hover:text-cyan-400">My Applications</Link>
                <Link to="/my-tickets" className="hover:text-cyan-400">My Tickets</Link>
              </>
            )}
            {token ? (
              <div className="flex items-center gap-4">
                <span className="text-slate-400">{user?.displayName || user?.email}</span>
                <button onClick={handleLogout} className="text-red-400 hover:text-red-300">Logout</button>
              </div>
            ) : (
              <>
                <Link to="/login" className="hover:text-cyan-400">Login</Link>
                <Link to="/register" className="hover:text-cyan-400">Register</Link>
              </>
            )}
          </div>
        </div>
      </nav>
      <main className="max-w-6xl mx-auto px-4 py-8">
        <Outlet />
      </main>
    </div>
  )
}
