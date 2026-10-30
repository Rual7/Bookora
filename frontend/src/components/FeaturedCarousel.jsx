import { useRef } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, ChevronLeft, ChevronRight } from 'lucide-react'
import BookCard from './BookCard.jsx'
import { t } from '../i18n/t.js'
import { useFetch } from '../hooks/useFetch.js'
import { useScrollEdges } from '../hooks/useScrollEdges.js'
import './Carousel.css'

const SKELETON_COUNT = 5

export default function FeaturedCarousel() {
  const { data: books, loading, error } = useFetch('/api/books/featured')
  const trackRef = useRef(null)
  const { atStart, atEnd, pageCount, currentPage, update, scrollByPage, scrollToPage } = useScrollEdges(trackRef, books)

  function onKeyDown(event) {
    if (event.key === 'ArrowRight') scrollByPage(1)
    if (event.key === 'ArrowLeft') scrollByPage(-1)
  }

  return (
    <section className="container section" aria-labelledby="featured-title">
      <div className="section-head">
        <h2 id="featured-title">{t('featured.title')}</h2>
        <Link to="/books" className="text-link">
          {t('featured.viewAll')}
          <ArrowRight size={16} strokeWidth={1.75} aria-hidden="true" />
        </Link>
      </div>

      {error && <p className="muted">{t('featured.error')}</p>}

      {!error && (
        <div
          className="carousel"
          role="region"
          aria-roledescription="carousel"
          aria-label={t('featured.region')}
          onKeyDown={onKeyDown}
        >
          <button
            type="button"
            className="carousel-btn carousel-prev"
            aria-label={t('featured.prev')}
            disabled={atStart}
            onClick={() => scrollByPage(-1)}
          >
            <ChevronLeft size={22} strokeWidth={1.75} aria-hidden="true" />
          </button>

          <ul className="carousel-track" ref={trackRef} onScroll={update}>
            {loading
              ? Array.from({ length: SKELETON_COUNT }, (_, index) => (
                  <li key={index} className="carousel-slide" aria-hidden="true">
                    <div className="book-card-skeleton">
                      <span className="skeleton skeleton-cover" />
                      <span className="skeleton skeleton-line" />
                      <span className="skeleton skeleton-line skeleton-short" />
                    </div>
                  </li>
                ))
              : books.map((book, index) => (
                  <li
                    key={book.id}
                    className="carousel-slide"
                    role="group"
                    aria-roledescription="slide"
                    aria-label={t('featured.slide', { index: index + 1, total: books.length })}
                  >
                    <BookCard book={book} />
                  </li>
                ))}
          </ul>

          <button
            type="button"
            className="carousel-btn carousel-next"
            aria-label={t('featured.next')}
            disabled={atEnd}
            onClick={() => scrollByPage(1)}
          >
            <ChevronRight size={22} strokeWidth={1.75} aria-hidden="true" />
          </button>
        </div>
      )}

      {!error && pageCount > 1 && (
        <div className="carousel-dots" role="group" aria-label={t('featured.pages')}>
          {Array.from({ length: pageCount }, (_, index) => (
            <button
              key={index}
              type="button"
              className={`carousel-dot${index === currentPage ? ' is-active' : ''}`}
              aria-label={t('featured.page', { number: index + 1 })}
              aria-current={index === currentPage}
              onClick={() => scrollToPage(index)}
            />
          ))}
        </div>
      )}
    </section>
  )
}
