import { useSearchParams } from 'react-router-dom'
import BookCard from '../components/BookCard.jsx'
import { t } from '../i18n/t.js'
import { usePageMeta } from '../hooks/usePageMeta.js'
import { useFetch } from '../hooks/useFetch.js'
import { useInfiniteList, useOnVisible } from '../hooks/useInfiniteList.js'
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
  const { items, total, loading, loadingMore, done, error, loadMore } = useInfiniteList(`/api/books?${params.toString()}`)
  const endOfList = useOnVisible(loadMore, items.length)

  const category = params.get('category') ?? ''
  const format = params.get('format') ?? ''
  const sort = params.get('sort') ?? 'title'

  function setFilter(name, value) {
    const next = new URLSearchParams(params)
    if (value) next.set(name, value)
    else next.delete(name)
    setParams(next)
  }

  const query = params.get('q')
  usePageMeta({
    title: query ? t('meta.booksSearch', { query }) : t('meta.books'),
    description: t('meta.booksDescription'),
    noindex: Boolean(query),
  })

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

      {loading && <p className="muted books-loading">{t('common.loading')}</p>}
      {error && <p className="muted">{t('books.error')}</p>}

      {!loading && !error && (
        <>
          <p className="muted results-count" aria-live="polite">
            {total === 0 ? t('books.none') : t('books.count', { count: total })}
          </p>
          <ul className="books-grid">
            {items.map((book) => (
              <li key={book.id}>
                <BookCard book={book} Heading="h2" />
              </li>
            ))}
          </ul>
          {loadingMore && <p className="muted books-more">{t('common.loading')}</p>}
          {!done && <div ref={endOfList} className="books-end" aria-hidden="true" />}
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
