import { useSearchParams } from 'react-router-dom'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import BookCard from '../components/BookCard.jsx'
import { t } from '../i18n/t.js'
import { useFetch } from '../hooks/useFetch.js'
import './BooksPage.css'

const FORMATS = [
  { value: '', label: 'filter.allFormats' },
  { value: 'printed', label: 'filter.printed' },
  { value: 'ebook', label: 'nav.ebooks' },
]

const SORTS = [
  { value: 'title', label: 'sort.title' },
  { value: 'newest', label: 'sort.newest' },
  { value: 'price-asc', label: 'sort.priceAsc' },
  { value: 'price-desc', label: 'sort.priceDesc' },
  { value: 'rating', label: 'sort.rating' },
]

export default function BooksPage() {
  const [params, setParams] = useSearchParams()
  const { data: categories } = useFetch('/api/categories')
  const { data, loading, error } = useFetch(`/api/books?${params.toString()}`)

  const category = params.get('category') ?? ''
  const format = params.get('format') ?? ''
  const sort = params.get('sort') ?? 'title'
  const page = Number(params.get('page') ?? 0)

  // Changing a filter always goes back to the first page.
  function setFilter(name, value) {
    const next = new URLSearchParams(params)
    if (value) next.set(name, value)
    else next.delete(name)
    next.delete('page')
    setParams(next)
  }

  function goToPage(number) {
    const next = new URLSearchParams(params)
    next.set('page', number)
    setParams(next)
    window.scrollTo({ top: 0 })
  }

  const query = params.get('q')

  return (
    <div className="container page">
      <h1>{query ? t('books.resultsFor', { query }) : t('nav.books')}</h1>

      <div className="filters">
        <Select label={t('filter.category')} value={category} onChange={(value) => setFilter('category', value)}>
          <option value="">{t('filter.allCategories')}</option>
          {categories?.map((item) => (
            <option key={item.id} value={item.slug}>{t(`category.${item.slug}`, {}, item.name)}</option>
          ))}
        </Select>
        <Select label={t('filter.format')} value={format} onChange={(value) => setFilter('format', value)}>
          {FORMATS.map((item) => (
            <option key={item.value} value={item.value}>{t(item.label)}</option>
          ))}
        </Select>
        <Select label={t('filter.sortBy')} value={sort} onChange={(value) => setFilter('sort', value === 'title' ? '' : value)}>
          {SORTS.map((item) => (
            <option key={item.value} value={item.value}>{t(item.label)}</option>
          ))}
        </Select>
      </div>

      {loading && <p className="muted">{t('common.loading')}</p>}
      {error && <p className="muted">{t('books.error')}</p>}

      {data && (
        <>
          <p className="muted results-count" aria-live="polite">
            {data.totalElements === 0 ? t('books.none') : t('books.count', { count: data.totalElements })}
          </p>
          <ul className="books-grid">
            {data.content.map((book) => (
              <li key={book.id}>
                <BookCard book={book} />
              </li>
            ))}
          </ul>
          {data.totalPages > 1 && (
            <nav className="pagination" aria-label={t('pagination.label')}>
              <button type="button" className="page-btn" disabled={page === 0}
                onClick={() => goToPage(page - 1)} aria-label={t('pagination.prev')}>
                <ChevronLeft size={18} aria-hidden="true" />
              </button>
              <span>{t('pagination.status', { page: page + 1, total: data.totalPages })}</span>
              <button type="button" className="page-btn" disabled={page + 1 >= data.totalPages}
                onClick={() => goToPage(page + 1)} aria-label={t('pagination.next')}>
                <ChevronRight size={18} aria-hidden="true" />
              </button>
            </nav>
          )}
        </>
      )}
    </div>
  )
}

function Select({ label, value, onChange, children }) {
  return (
    <label className="filter">
      <span className="filter-label">{label}</span>
      <select value={value} onChange={(event) => onChange(event.target.value)}>
        {children}
      </select>
    </label>
  )
}
