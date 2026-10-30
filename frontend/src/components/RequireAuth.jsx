import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

/** Shows the page only to signed-in users; guests are sent to the login page and brought back after. */
export default function RequireAuth({ children }) {
  const { user } = useAuth()
  const location = useLocation()

  if (user === undefined) return null
  if (user === null) return <Navigate to="/login" state={{ from: location.pathname }} replace />
  return children
}
