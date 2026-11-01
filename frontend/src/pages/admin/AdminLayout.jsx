import { useEffect, useRef } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import { t } from '../../i18n/t.js'
import { useScrollEdges } from '../../hooks/useScrollEdges.js'
import './AdminLayout.css'

const SECTIONS = [
  { to: '/admin/books', label: 'admin.books' },
  { to: '/admin/orders', label: 'admin.orders' },
  { to: '/admin/reviews', label: 'admin.reviews' },
  { to: '/admin/messages', label: 'admin.messages' },
  { to: '/admin/customers', label: 'admin.customers' },
]

export default function AdminLayout() {
  const rowRef = useRef(null)
  const { pathname } = useLocation()
  const { atStart, atEnd, update, scrollByPage } = useScrollEdges(rowRef)
  const scrolls = !(atStart && atEnd)

  // On a phone the open section may be off screen, so bring its tab into view.
  useEffect(() => {
    rowRef.current?.querySelector('.active')?.scrollIntoView({ block: 'nearest', inline: 'center' })
  }, [pathname])

  return (
    <div className="container page">
      <h1>{t('admin.title')}</h1>
      <div className="admin-nav-wrap">
        {scrolls && (
          <button type="button" className="admin-nav-btn admin-nav-prev" aria-label={t('strip.prev')}
            disabled={atStart} onClick={() => scrollByPage(-1)}>
            <ChevronLeft size={18} aria-hidden="true" />
          </button>
        )}
        <nav className="admin-nav" aria-label={t('admin.title')} ref={rowRef} onScroll={update}>
          {SECTIONS.map((section) => (
            <NavLink key={section.to} to={section.to}>{t(section.label)}</NavLink>
          ))}
        </nav>
        {scrolls && (
          <button type="button" className="admin-nav-btn admin-nav-next" aria-label={t('strip.next')}
            disabled={atEnd} onClick={() => scrollByPage(1)}>
            <ChevronRight size={18} aria-hidden="true" />
          </button>
        )}
      </div>
      <Outlet />
    </div>
  )
}
