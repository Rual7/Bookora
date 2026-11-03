import { useState } from 'react'
import { Link } from 'react-router-dom'
import { BookOpen, Download, Eye } from 'lucide-react'
import AccountNav from '../components/AccountNav.jsx'
import Cover from '../components/Cover.jsx'
import EmptyState from '../components/EmptyState.jsx'
import { downloadFile } from '../api/client.js'
import { t } from '../i18n/t.js'
import { usePageMeta } from '../hooks/usePageMeta.js'
import { useFetch } from '../hooks/useFetch.js'
import { formatDate } from '../utils/format.js'
import './LibraryPage.css'

/** The e-books a customer has paid for, with a button to save each one and, for PDFs, to read it in the browser. */
export default function LibraryPage() {
  usePageMeta({ title: t('meta.ebooks'), noindex: true })

  const { data: items, error, loading } = useFetch('/api/library')
  const [problem, setProblem] = useState('')

  async function download(item) {
    setProblem('')
    try {
      await downloadFile(`/api/downloads/${item.orderItemId}`)
    } catch (failure) {
      setProblem(failure.message)
    }
  }

  return (
    <div className="container page">
      <h1>{t('nav.account')}</h1>
      <AccountNav />

      {(error || problem) && <p className="form-error" role="alert">{problem || error.message}</p>}
      {loading && <p className="muted">{t('common.loading')}</p>}
      {items?.length === 0 && (
        <EmptyState icon={BookOpen} text={t('library.none')}>
          <Link to="/books?format=ebook" className="btn">{t('library.browse')}</Link>
        </EmptyState>
      )}

      <ul className="library-list">
        {items?.map((item) => (
          <li key={item.orderItemId} className="library-item">
            <Link to={`/books/${item.bookSlug}`} className="library-cover" aria-hidden="true" tabIndex={-1}>
              <Cover book={{ ...item, id: item.orderItemId }} />
            </Link>
            <div className="library-info">
              <Link to={`/books/${item.bookSlug}`} className="library-title">{item.title}</Link>
              <p className="muted">{item.authors.join(', ')}</p>
              <p className="muted">
                {t(`format.${item.format}`)} &middot; {t('library.bought')} <time dateTime={item.purchasedAt}>{formatDate(item.purchasedAt)}</time>
              </p>
              {item.available ? (
                <div className="library-actions">
                  <button type="button" className="btn btn-small" onClick={() => download(item)}
                    aria-label={t('order.downloadLabel', { title: item.title })}>
                    <Download size={16} strokeWidth={1.5} aria-hidden="true" /> {t('order.download')}
                  </button>
                  {item.format === 'PDF' && (
                    <a href={`/api/downloads/${item.orderItemId}?inline=true`} target="_blank" rel="noopener"
                      className="btn btn-small btn-outline" aria-label={t('library.readLabel', { title: item.title })}>
                      <Eye size={16} strokeWidth={1.5} aria-hidden="true" /> {t('library.read')}
                    </a>
                  )}
                </div>
              ) : (
                <p className="library-pending">{t('library.pending')}</p>
              )}
            </div>
          </li>
        ))}
      </ul>

      {items?.some((item) => item.format === 'EPUB' && item.available) && (
        <p className="muted library-note">{t('library.epubNote')}</p>
      )}
    </div>
  )
}
