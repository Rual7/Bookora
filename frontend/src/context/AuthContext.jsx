import { createContext, useContext, useEffect, useState } from 'react'
import { api } from '../api/client.js'
import { captchaHeaders } from '../utils/captcha.js'

const AuthContext = createContext(null)

/** `user` is undefined while we check the session, null for a guest, or the signed-in user. */
export function AuthProvider({ children }) {
  const [user, setUser] = useState(undefined)

  useEffect(() => {
    api
      .get('/api/auth/me')
      .then(setUser)
      .catch(() => setUser(null))
  }, [])

  async function login(email, password, captchaToken) {
    setUser(await api.post('/api/auth/login', { email, password }, captchaHeaders(captchaToken)))
  }

  /** The server signs the new customer in, because a CAPTCHA token can only be used once. */
  async function register(form, captchaToken) {
    setUser(await api.post('/api/auth/register', form, captchaHeaders(captchaToken)))
  }

  async function logout() {
    await api.post('/api/auth/logout')
    setUser(null)
  }

  return <AuthContext value={{ user, login, register, logout }}>{children}</AuthContext>
}

export function useAuth() {
  return useContext(AuthContext)
}
