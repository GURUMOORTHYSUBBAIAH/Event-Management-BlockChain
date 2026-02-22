import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import Layout from './components/Layout'
import Login from './pages/Login'
import Register from './pages/Register'
import Events from './pages/Events'
import EventDetail from './pages/EventDetail'
import MyApplications from './pages/MyApplications'
import MyTickets from './pages/MyTickets'
import Dashboard from './pages/Dashboard'
import PaymentSuccess from './pages/PaymentSuccess'
import OAuth2Callback from './pages/OAuth2Callback'

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const { token } = useAuth()
  if (!token) return <Navigate to="/login" replace />
  return <>{children}</>
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/oauth2/callback" element={<OAuth2Callback />} />
          <Route path="/" element={<Layout />}>
            <Route index element={<Events />} />
            <Route path="events/:id" element={<EventDetail />} />
            <Route path="my-applications" element={<PrivateRoute><MyApplications /></PrivateRoute>} />
            <Route path="my-tickets" element={<PrivateRoute><MyTickets /></PrivateRoute>} />
            <Route path="dashboard/:eventId" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
            <Route path="payment/success" element={<PaymentSuccess />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
