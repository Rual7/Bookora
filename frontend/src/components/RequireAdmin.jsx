import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import NotFoundPage from '../pages/NotFoundPage.jsx'

/** Shows the page only to administrators; guests go to the login page, customers see "not found". */
export default function RequireAdmin({ children }) {
  const { user } = useAuth()

  if (user === undefined) return null
  if (user === null) return <Navigate to="/login" replace />
  if (user.role !== 'ADMIN') return <NotFoundPage />
  return children
}
