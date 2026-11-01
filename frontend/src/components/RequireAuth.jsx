import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

/** Shows the page only to signed-in users; guests are sent to the login page. */
export default function RequireAuth({ children }) {
  const { user } = useAuth()

  if (user === undefined) return null
  if (user === null) return <Navigate to="/login" replace />
  return children
}
