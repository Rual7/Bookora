import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Search } from 'lucide-react'
import Cover from './Cover.jsx'
import { t } from '../i18n/t.js'
import { formatPrice } from '../utils/format.js'
import { useFetch } from '../hooks/useFetch.js'
import './SearchBox.css'

const MIN_LENGTH = 2
const MAX_RESULTS = 5

/**
 * Search field of the header. Matching books appear in a panel under the header while you type.
 * The header decides when search is `active`, because it must not be open together with the menu.
 */
export default function SearchBox({ active, onActivate, onDeactivate }) {
  const navigate = useNavigate()
  const boxRef = useRef(null)
  const inputRef = useRef(null)
  const [text, setText] = useState('')
  const [term, setTerm] = useState('')

  // Wait a moment after the last key press before asking the server.
  useEffect(() => {
    const timer = setTimeout(() => setTerm(text.trim()), 300)
    return () => clearTimeout(timer)
  }, [text])

  // Close on a click outside (the header's search icon handles its own clicks), and on Escape.
  useEffect(() => {
    function onPointerDown(event) {
      const inside = boxRef.current?.contains(event.target) || event.target.closest('[data-search-toggle]')
      if (!inside) onDeactivate()
    }
    function onKeyDown(event) {
      if (event.key === 'Escape') onDeactivate()
    }
    document.addEventListener('pointerdown', onPointerDown)
    document.addEventListener('keydown', onKeyDown)
    return () => {
      document.removeEventListener('pointerdown', onPointerDown)
      document.removeEventListener('keydown', onKeyDown)
    }
  }, [onDeactivate])

  // On a phone the field appears when the search icon is pressed, so put the cursor in it.
  useEffect(() => {
    if (active && window.matchMedia('(max-width: 519px)').matches) inputRef.current?.focus()
  }, [active])

  const path = term.length >= MIN_LENGTH ? `/api/books?q=${encodeURIComponent(term)}&size=${MAX_RESULTS}` : null
  const { data, loading, error } = useFetch(path)
  const typed = text.trim()
  const showPanel = active && typed.length >= MIN_LENGTH
  const upToDate = term === typed

  function submit(event) {
    event.preventDefault()
    if (!typed) return
    onDeactivate()
    navigate(`/books?q=${encodeURIComponent(typed)}`)
  }

  return (
    <div ref={boxRef} className={`search${active ? ' is-open' : ''}`}>
      <div className="search-bar">
        <form className="search-form" role="search" onSubmit={submit}>
          <Search size={18} strokeWidth={1.5} aria-hidden="true" />
          <input ref={inputRef} type="search" value={text} placeholder={t('search.placeholder')}
            aria-label={t('search.label')} autoComplete="off" onFocus={onActivate}
            onChange={(event) => { setText(event.target.value); onActivate() }} />
        </form>
      </div>

      {showPanel && (
        <div className="search-panel" aria-live="polite">
          <div className="container">
            {(loading || !upToDate) && <p className="search-message">{t('search.searching')}</p>}
            {error && <p className="search-message">{t('search.unavailable')}</p>}
            {data && upToDate && data.content.length === 0 && (
              <p className="search-message">{t('search.none', { term })}</p>
            )}
            {data && upToDate && data.content.length > 0 && (
              <>
                <ul className="search-results">
                  {data.content.map((book) => (
                    <li key={book.id}>
                      <Link to={`/books/${book.slug}`} className="search-result" onClick={onDeactivate}>
                        <span className="search-cover"><Cover book={book} /></span>
                        <span>
                          <strong>{book.title}</strong>
                          <span className="muted">{book.authors.join(', ')}</span>
                        </span>
                        {book.price != null && <span className="search-price">{formatPrice(book.price)}</span>}
                      </Link>
                    </li>
                  ))}
                </ul>
                {data.totalElements > MAX_RESULTS && (
                  <Link to={`/books?q=${encodeURIComponent(term)}`} className="text-link search-all"
                    onClick={onDeactivate}>
                    {t('search.seeAll', { count: data.totalElements })}
                  </Link>
                )}
              </>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
