import { useSearchParams } from 'react-router-dom'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import BookCard from '../components/BookCard.jsx'
import { useFetch } from '../hooks/useFetch.js'
import './BooksPage.css'

const FORMATS = [
  { value: '', label: 'All formats' },
  { value: 'printed', label: 'Printed' },
  { value: 'ebook', label: 'E-books' },
]

const SORTS = [
  { value: 'title', label: 'Title A-Z' },
  { value: 'newest', label: 'Newest first' },
  { value: 'price-asc', label: 'Price: low to high' },
  { value: 'price-desc', label: 'Price: high to low' },
  { value: 'rating', label: 'Top rated' },
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
      <h1>{query ? `Results for "${query}"` : 'Books'}</h1>

      <div className="filters">
        <Select label="Category" value={category} onChange={(value) => setFilter('category', value)}>
          <option value="">All categories</option>
          {categories?.map((item) => (
            <option key={item.id} value={item.slug}>{item.name}</option>
          ))}
        </Select>
        <Select label="Format" value={format} onChange={(value) => setFilter('format', value)}>
          {FORMATS.map((item) => (
            <option key={item.value} value={item.value}>{item.label}</option>
          ))}
        </Select>
        <Select label="Sort by" value={sort} onChange={(value) => setFilter('sort', value === 'title' ? '' : value)}>
          {SORTS.map((item) => (
            <option key={item.value} value={item.value}>{item.label}</option>
          ))}
        </Select>
      </div>

      {loading && <p className="muted">Loading...</p>}
      {error && <p className="muted">The catalog could not be loaded. Please try again later.</p>}

      {data && (
        <>
          <p className="muted results-count" aria-live="polite">
            {data.totalElements === 0
              ? 'No books match your filters.'
              : `${data.totalElements} ${data.totalElements === 1 ? 'book' : 'books'}`}
          </p>
          <ul className="books-grid">
            {data.content.map((book) => (
              <li key={book.id}>
                <BookCard book={book} />
              </li>
            ))}
          </ul>
          {data.totalPages > 1 && (
            <nav className="pagination" aria-label="Pages">
              <button type="button" className="page-btn" disabled={page === 0}
                onClick={() => goToPage(page - 1)} aria-label="Previous page">
                <ChevronLeft size={18} aria-hidden="true" />
              </button>
              <span>Page {page + 1} of {data.totalPages}</span>
              <button type="button" className="page-btn" disabled={page + 1 >= data.totalPages}
                onClick={() => goToPage(page + 1)} aria-label="Next page">
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
