import { createContext, ReactNode, useContext, useState } from 'react'

const API = 'http://localhost:8080/api'

interface User {
  id: number
  email: string
  displayName?: string
  walletAddress?: string
  profileImageUrl?: string
}

interface AuthContextType {
  token: string | null
  refreshToken: string | null
  user: User | null
  roles: string[]
  login: (email: string, password: string) => Promise<void>
  register: (email: string, password: string, displayName?: string) => Promise<void>
  logout: () => void
  refreshAuth: () => Promise<void>
  setAuth: (data: { accessToken: string; refreshToken: string; user: User; roles: string[] }) => void
}

const AuthContext = createContext<AuthContextType | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('accessToken'))
  const [refreshToken, setRefreshToken] = useState<string | null>(() => localStorage.getItem('refreshToken'))
  const [user, setUser] = useState<User | null>(() => {
    const u = localStorage.getItem('user')
    return u ? JSON.parse(u) : null
  })
  const [roles, setRoles] = useState<string[]>(() => {
    const r = localStorage.getItem('roles')
    return r ? JSON.parse(r) : []
  })

  const setAuth = (data: { accessToken: string; refreshToken: string; user: User; roles: string[] }) => {
    setToken(data.accessToken)
    setRefreshToken(data.refreshToken)
    setUser(data.user)
    setRoles(data.roles)
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    localStorage.setItem('user', JSON.stringify(data.user))
    localStorage.setItem('roles', JSON.stringify(data.roles))
  }

  const logout = () => {
    setToken(null)
    setRefreshToken(null)
    setUser(null)
    setRoles([])
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
    localStorage.removeItem('roles')
  }

  const refreshAuth = async () => {
    const rt = localStorage.getItem('refreshToken')
    if (!rt) return
    const res = await fetch(`${API}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: rt }),
    })
    if (res.ok) {
      const data = await res.json()
      setAuth(data)
    } else {
      logout()
    }
  }

  const login = async (email: string, password: string) => {
    const res = await fetch(`${API}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    })
    if (!res.ok) throw new Error((await res.json()).message || 'Login failed')
    const data = await res.json()
    setAuth(data)
  }

  const register = async (email: string, password: string, displayName?: string) => {
    const res = await fetch(`${API}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password, displayName }),
    })
    if (!res.ok) throw new Error((await res.json()).message || 'Registration failed')
    const data = await res.json()
    setAuth(data)
  }

  return (
    <AuthContext.Provider value={{ token, refreshToken, user, roles, login, register, logout, refreshAuth, setAuth }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
