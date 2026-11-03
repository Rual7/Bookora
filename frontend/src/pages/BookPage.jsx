import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { BookX, Star } from 'lucide-react'
import Cover from '../components/Cover.jsx'
import StatusPage from '../components/StatusPage.jsx'
import ReviewForm from '../components/ReviewForm.jsx'
import WishlistButton from '../components/WishlistButton.jsx'
import { useCart } from '../context/CartContext.jsx'
import { t } from '../i18n/t.js'
import { formatPrice } from '../utils/format.js'
import { usePanel } from '../context/PanelContext.jsx'
import { useFetch } from '../hooks/useFetch.js'
import { shorten, usePageMeta } from '../hooks/usePageMeta.js'
import './BookPage.css'

/** The title and description of a book page; a missing book or a failed load stays out of search results. */
function bookMeta(book, error) {
  if (!book) return { title: error?.status === 404 ? t('book.notFound') : t('meta.books'), noindex: true }
  const vars = { title: book.title, authors: book.authors.join(', ') }
  return {
    title: t('meta.book', vars),
    description: book.description ? shorten(book.description) : t('meta.bookDescription', vars),
  }
}

export default function BookPage() {
  const { slug } = useParams()
  const { data: book, loading, error } = useFetch(`/api/books/${encodeURIComponent(slug)}`)
  usePageMeta(bookMeta(book, error))

  if (loading) return <div className="container page muted">{t('common.loading')}</div>
  if (error) {
    const missing = error.status === 404
    return (
      <StatusPage icon={BookX} code={missing ? '404' : undefined} title={missing ? t('book.notFound') : t('common.wrong')}
        text={missing ? t('book.notFoundText') : t('error.loadText')}>
        <Link to="/books" className="btn">{t('book.backToAll')}</Link>
      </StatusPage>
    )
  }
  // key makes the format choice start fresh when the visitor moves to another book
  return <BookDetail key={book.id} book={book} />
}

function BookDetail({ book }) {
  const { addItem } = useCart()
  const { openPanel } = usePanel()

  const [productId, setProductId] = useState(book.products[0]?.id)
  const [imageIndex, setImageIndex] = useState(0)
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)

  const product = book.products.find((item) => item.id === productId)

  async function addToCart() {
    setBusy(true)
    setMessage('')
    try {
      await addItem(product.id)
      openPanel('cart')
    } catch (problem) {
      setMessage(problem.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="container page book-detail">
      <div className="book-detail-cover">
        <Cover book={{ ...book, coverImagePath: book.images[imageIndex] ?? book.coverImagePath }} />
        {book.images.length > 1 && (
          <ul className="book-thumbs" aria-label={t('book.images')}>
            {book.images.map((path, index) => (
              <li key={path}>
                <button type="button" className={`book-thumb${index === imageIndex ? ' is-selected' : ''}`}
                  aria-label={t('book.showImage', { number: index + 1 })} aria-pressed={index === imageIndex}
                  onClick={() => setImageIndex(index)}>
                  <img src={path} alt="" width="60" height="90" loading="lazy" />
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>

      <div>
        <h1>{book.title}</h1>
        <p className="book-detail-author">{book.authors.join(', ')}</p>
        <Rating average={book.averageRating} count={book.reviewCount} />

        {product ? (
          <fieldset className="format-picker">
            <legend>{t('filter.format')}</legend>
            {book.products.map((item) => (
              <label key={item.id} className={`format-option${item.id === productId ? ' is-selected' : ''}`}>
                <input
                  type="radio"
                  name="format"
                  value={item.id}
                  checked={item.id === productId}
                  onChange={() => setProductId(item.id)}
                />
                <span>{t(`format.${item.format}`)}</span>
                <strong>{formatPrice(item.price)}</strong>
              </label>
            ))}
          </fieldset>
        ) : (
          <p className="muted">{t('book.unavailable')}</p>
        )}

        {product && (
          <div className="buy-row">
            <button type="button" className="btn" onClick={addToCart} disabled={busy || !product.inStock}>
              {product.inStock ? t('book.addToCart') : t('availability.OUT_OF_STOCK')}
            </button>
            <WishlistButton book={book} className="wish-btn-inline" />
          </div>
        )}
        {message && <p className="form-error" role="alert">{message}</p>}

        <p className="book-detail-description">{book.description}</p>

        <dl className="book-facts">
          {book.publisher && <><dt>{t('book.publisher')}</dt><dd>{book.publisher}</dd></>}
          {book.pageCount && <><dt>{t('book.pages')}</dt><dd>{book.pageCount}</dd></>}
          {book.categories.length > 0 && (
            <>
              <dt>{t('nav.categories')}</dt>
              <dd>
                {book.categories.map((category, index) => (
                  <span key={category.id}>
                    {index > 0 && ', '}
                    <Link to={`/books?category=${category.slug}`} className="text-link">
                      {t(`category.${category.slug}`, {}, category.name)}
                    </Link>
                  </span>
                ))}
              </dd>
            </>
          )}
        </dl>

        <section className="reviews" aria-labelledby="reviews-title">
          <h2 id="reviews-title">{t('book.reviews')}</h2>
          {book.reviews.length === 0 && <p className="muted">{t('book.noReviews')}</p>}
          {book.reviews.map((review, index) => (
            <article key={index} className="review">
              <Stars value={review.rating} />
              {review.title && <h3>{review.title}</h3>}
              {review.comment && <p>{review.comment}</p>}
              <p className="muted review-author">
                {review.authorName}
                {review.verifiedPurchase && <span className="status status-clean">{t('review.verified')}</span>}
              </p>
            </article>
          ))}
          <ReviewForm slug={book.slug} />
        </section>
      </div>
    </div>
  )
}

function Rating({ average, count }) {
  if (!count) return <p className="muted rating">{t('book.noRatings')}</p>
  return (
    <p className="rating">
      <Stars value={Math.round(average)} />
      <span className="muted">{t('book.ratingSummary', { average: Number(average).toFixed(1), count })}</span>
    </p>
  )
}

function Stars({ value }) {
  return (
    <span className="stars" role="img" aria-label={t('book.stars', { value })}>
      {[1, 2, 3, 4, 5].map((n) => (
        <Star key={n} size={16} strokeWidth={1.5} className={n <= value ? 'star-on' : 'star-off'} aria-hidden="true" />
      ))}
    </span>
  )
}
