import { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function OAuth2Callback() {
  const [params] = useSearchParams()
  const { setAuth } = useAuth()
  const navigate = useNavigate()

  useEffect(() => {
    const accessToken = params.get('access_token')
    const refreshToken = params.get('refresh_token')
    if (accessToken && refreshToken) {
      const payload = JSON.parse(atob(accessToken.split('.')[1]))
      setAuth({
        accessToken,
        refreshToken,
        user: {
          id: parseInt(payload.sub, 10),
          email: payload.email || '',
          displayName: payload.email?.split('@')[0],
        },
        roles: payload.roles || ['USER'],
      })
      navigate('/')
    } else {
      navigate('/login')
    }
  }, [params, setAuth, navigate])

  return <div className="flex justify-center py-20">Completing sign in...</div>
}
