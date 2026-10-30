import { useCallback, useEffect, useState } from 'react'
import { Link, NavLink } from 'react-router-dom'
import { Heart, Menu, Search, ShoppingCart, User, X } from 'lucide-react'
import Logo from './Logo.jsx'
import SearchBox from './SearchBox.jsx'
import ThemeToggle from './ThemeToggle.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { useCart } from '../context/CartContext.jsx'
import { t } from '../i18n/t.js'
import { usePanel } from '../context/PanelContext.jsx'
import { useWishlist } from '../context/WishlistContext.jsx'
import './Header.css'

const NAV = [
  { to: '/', label: 'nav.home', end: true },
  { to: '/books', label: 'nav.books', end: true },
  { to: '/books?format=ebook', label: 'nav.ebooks', end: false },
  { to: '/categories', label: 'nav.categories', end: false },
  { to: '/about', label: 'nav.about', end: false },
]

export default function Header() {
  const { user, logout } = useAuth()
  const { itemCount } = useCart()
  const { books: wishedBooks } = useWishlist()
  const { openPanel } = usePanel()
  // What is open under the header: the menu, the search, or nothing. Only one at a time.
  const [open, setOpen] = useState(null)
  const [scrolled, setScrolled] = useState(false)

  const menuOpen = open === 'menu'
  const searchOpen = open === 'search'
  const toggle = (name) => setOpen((current) => (current === name ? null : name))
  const activateSearch = useCallback(() => setOpen('search'), [])
  const deactivateSearch = useCallback(() => setOpen((current) => (current === 'search' ? null : current)), [])

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 4)
    onScroll()
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  return (
    <header className={`site-header${scrolled ? ' is-scrolled' : ''}`}>
      <div className="container header-inner">
        <Logo />

        <nav id="primary-nav" className={`primary-nav${menuOpen ? ' is-open' : ''}`} aria-label={t('nav.primary')}>
          {NAV.map((item) => (
            <NavLink
              key={item.label}
              to={item.to}
              end={item.end}
              className={({ isActive }) => `nav-link${isActive && item.label !== 'nav.ebooks' ? ' is-active' : ''}`}
              onClick={() => setOpen(null)}
            >
              {t(item.label)}
            </NavLink>
          ))}
          {/* On a phone the account buttons move into this menu to leave room in the header. */}
          {user ? (
            <>
              <NavLink to="/orders" className="nav-link nav-account" onClick={() => setOpen(null)}>
                {t('nav.account')}
              </NavLink>
              <button type="button" className="nav-link nav-account" onClick={() => { setOpen(null); logout() }}>
                {t('nav.signOut')}
              </button>
            </>
          ) : (
            <NavLink to="/login" className="nav-link nav-account" onClick={() => setOpen(null)}>
              {t('nav.signIn')}
            </NavLink>
          )}
        </nav>

        <SearchBox active={searchOpen} onActivate={activateSearch} onDeactivate={deactivateSearch} />

        <div className="header-actions">
          <button type="button" className="icon-btn search-toggle" data-search-toggle aria-label={t('nav.search')}
            aria-expanded={searchOpen} onClick={() => toggle('search')}>
            <Search size={22} strokeWidth={1.5} aria-hidden="true" />
          </button>
          <ThemeToggle className="icon-btn" />
          <button type="button" className="icon-btn badge-host" aria-haspopup="dialog"
            aria-label={t('nav.wishlist', { count: wishedBooks.length })} onClick={() => openPanel('wishlist')}>
            <Heart size={22} strokeWidth={1.5} aria-hidden="true" />
            {wishedBooks.length > 0 && <span className="badge" aria-hidden="true">{wishedBooks.length}</span>}
          </button>
          <button type="button" className="icon-btn badge-host" aria-haspopup="dialog"
            aria-label={t('nav.cart', { count: itemCount })} onClick={() => openPanel('cart')}>
            <ShoppingCart size={22} strokeWidth={1.5} aria-hidden="true" />
            {itemCount > 0 && <span className="badge" aria-hidden="true">{itemCount}</span>}
          </button>
          <div className="account-actions">
            {user ? (
              <Link to="/orders" className="icon-btn" aria-label={t('nav.account')}>
                <User size={22} strokeWidth={1.5} aria-hidden="true" />
              </Link>
            ) : (
              <Link to="/login" className="icon-btn" aria-label={t('nav.signIn')}>
                <User size={22} strokeWidth={1.5} aria-hidden="true" />
              </Link>
            )}
          </div>
          <button
            type="button"
            className="icon-btn menu-toggle"
            aria-label={menuOpen ? t('nav.closeMenu') : t('nav.openMenu')}
            aria-expanded={menuOpen}
            aria-controls="primary-nav"
            onClick={() => toggle('menu')}
          >
            {menuOpen ? <X size={22} strokeWidth={1.5} /> : <Menu size={22} strokeWidth={1.5} />}
          </button>
        </div>
      </div>
    </header>
  )
}
