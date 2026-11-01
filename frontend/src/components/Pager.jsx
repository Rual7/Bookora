import { ChevronLeft, ChevronRight } from 'lucide-react'
import { t } from '../i18n/t.js'
import './Pager.css'

/** Previous / next buttons for a paged list. `page` starts at 0. Renders nothing when there is one page. */
export default function Pager({ page, totalPages, onChange }) {
  if (totalPages <= 1) return null

  return (
    <nav className="pagination" aria-label={t('pagination.label')}>
      <button type="button" className="page-btn" disabled={page === 0}
        onClick={() => onChange(page - 1)} aria-label={t('pagination.prev')}>
        <ChevronLeft size={18} aria-hidden="true" />
      </button>
      <span>{t('pagination.status', { page: page + 1, total: totalPages })}</span>
      <button type="button" className="page-btn" disabled={page + 1 >= totalPages}
        onClick={() => onChange(page + 1)} aria-label={t('pagination.next')}>
        <ChevronRight size={18} aria-hidden="true" />
      </button>
    </nav>
  )
}
