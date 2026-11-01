import { useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { t } from '../../i18n/t.js'
import { useInfiniteList, useOnVisible } from '../../hooks/useInfiniteList.js'
import './Admin.css'

export default function AdminBooksPage() {
  const [typed, setTyped] = useState('')
  const [query, setQuery] = useState('')
  const scrollBox = useRef(null)
  const { items, total, loading, loadingMore, done, error, loadMore } = useInfiniteList(
    `/api/admin/books?q=${encodeURIComponent(query)}`,
  )
  const endOfList = useOnVisible(loadMore, items.length, scrollBox)

  function search(event) {
    event.preventDefault()
    setQuery(typed.trim())
  }

  return (
    <>
      <div className="admin-toolbar">
        <form className="admin-search" onSubmit={search} role="search">
          <div className="field">
            <label className="field-label" htmlFor="admin-book-search">{t('admin.search')}</label>
            <input id="admin-book-search" type="search" value={typed} onChange={(event) => setTyped(event.target.value)} />
          </div>
          <button type="submit" className="btn btn-small btn-outline">{t('admin.searchButton')}</button>
        </form>
        <Link to="/admin/books/new" className="btn btn-small admin-toolbar-end">{t('admin.addBook')}</Link>
      </div>

      {error && <p className="form-error" role="alert">{error.message}</p>}
      {loading && <p className="muted">{t('admin.loading')}</p>}
      {!loading && !error && (
        <p className="muted admin-count" aria-live="polite">
          {total === 0 ? t('admin.empty') : t('books.count', { count: total })}
        </p>
      )}

      {items.length > 0 && (
        <div className="table-wrap admin-scroll" ref={scrollBox} tabIndex={0} aria-label={t('admin.booksCaption')}>
          <table className="data-table admin-table">
            <caption className="visually-hidden">{t('admin.booksCaption')}</caption>
            <thead>
              <tr>
                <th scope="col">{t('admin.col.title')}</th>
                <th scope="col">{t('admin.col.authors')}</th>
                <th scope="col">{t('admin.col.formats')}</th>
                <th scope="col">{t('admin.col.status')}</th>
              </tr>
            </thead>
            <tbody>
              {items.map((book) => (
                <tr key={book.id}>
                  <th scope="row">
                    <Link to={`/admin/books/${book.id}`} className="text-link">{book.title}</Link>
                  </th>
                  <td data-label={t('admin.col.authors')}>{book.authors.join(', ')}</td>
                  <td data-label={t('admin.col.formats')}>{book.formats.map((format) => t(`format.${format}`)).join(', ')}</td>
                  <td data-label={t('admin.col.status')}>
                    <span className={`status ${book.active ? 'status-clean' : 'status-cancelled'}`}>
                      {book.active ? t('admin.visible') : t('admin.hidden')}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {loadingMore && <p className="muted admin-more">{t('common.loading')}</p>}
          {!done && <div ref={endOfList} className="admin-end" aria-hidden="true" />}
        </div>
      )}
    </>
  )
}
