import { useState } from 'react'
import { Link } from 'react-router-dom'
import Pager from '../../components/Pager.jsx'
import { api } from '../../api/client.js'
import { t } from '../../i18n/t.js'
import { useFetch } from '../../hooks/useFetch.js'
import { formatDate } from '../../utils/format.js'
import './Admin.css'
import './AdminReviewsPage.css'

const STATUSES = ['PENDING', 'APPROVED', 'REJECTED']

export default function AdminReviewsPage() {
  const [status, setStatus] = useState('PENDING')
  const [page, setPage] = useState(0)
  const [problem, setProblem] = useState('')
  const { data, error, loading, reload } = useFetch(`/api/admin/reviews?status=${status}&page=${page}`)

  function filter(event) {
    setStatus(event.target.value)
    setPage(0)
  }

  async function moderate(id, next) {
    setProblem('')
    try {
      await api.put(`/api/admin/reviews/${id}/status`, { status: next })
      reload()
    } catch (failure) {
      setProblem(failure.message)
    }
  }

  return (
    <>
      <div className="admin-toolbar">
        <label className="filter">
          <span className="filter-label">{t('admin.col.status')}</span>
          <select value={status} onChange={filter}>
            {STATUSES.map((item) => <option key={item} value={item}>{t(`admin.reviewStatus.${item}`)}</option>)}
          </select>
        </label>
      </div>

      {(error || problem) && <p className="form-error" role="alert">{problem || error.message}</p>}
      {loading && <p className="muted">{t('admin.loading')}</p>}
      {data?.content.length === 0 && <p className="muted">{t('admin.empty')}</p>}

      <ul className="admin-reviews">
        {data?.content.map((review) => (
          <li key={review.id} className="admin-review">
            <p className="admin-review-head">
              <strong>{review.rating} / 5</strong>
              {review.title && <span>{review.title}</span>}
            </p>
            <p>{review.comment}</p>
            <p className="muted admin-review-meta">
              {review.authorName} ({review.authorEmail}) {t('admin.reviewOf')}{' '}
              <Link to={`/books/${review.bookSlug}`} className="text-link">{review.bookTitle}</Link>
              {' '}&middot; <time dateTime={review.createdAt}>{formatDate(review.createdAt)}</time>
            </p>
            <p>
              <span className={`status ${review.verifiedPurchase ? 'status-clean' : ''}`}>
                {review.verifiedPurchase ? t('admin.verified') : t('admin.notBuyer')}
              </span>
            </p>
            <div className="admin-actions">
              {review.status !== 'APPROVED' && (
                <button type="button" className="btn btn-small" onClick={() => moderate(review.id, 'APPROVED')}>{t('admin.approve')}</button>
              )}
              {review.status !== 'REJECTED' && (
                <button type="button" className="btn btn-small btn-outline" onClick={() => moderate(review.id, 'REJECTED')}>{t('admin.reject')}</button>
              )}
            </div>
          </li>
        ))}
      </ul>

      {data && <Pager page={page} totalPages={data.totalPages} onChange={setPage} />}
    </>
  )
}
