import { NavLink } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { t } from '../i18n/t.js'
import './AccountNav.css'

export default function AccountNav() {
  const { logout } = useAuth()

  return (
    <nav className="account-nav" aria-label={t('nav.account')}>
      <NavLink to="/orders">{t('footer.orders')}</NavLink>
      <NavLink to="/addresses">{t('footer.addresses')}</NavLink>
      <button type="button" className="account-nav-signout" onClick={logout}>{t('nav.signOut')}</button>
    </nav>
  )
}
