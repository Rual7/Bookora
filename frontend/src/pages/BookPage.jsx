import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { Star } from 'lucide-react'
import Cover from '../components/Cover.jsx'
import WishlistButton from '../components/WishlistButton.jsx'
import { FORMAT_NAMES, formatPrice } from '../utils/format.js'
import { useCart } from '../context/CartContext.jsx'
import { usePanel } from '../context/PanelContext.jsx'
import { useFetch } from '../hooks/useFetch.js'
import './BookPage.css'

export default function BookPage() {
  const { slug } = useParams()
  const { data: book, loading, error } = useFetch(`/api/books/${encodeURIComponent(slug)}`)

  if (loading) return <div className="container page muted">Loading...</div>
  if (error) {
    return (
      <div className="container page">
        <h1>{error.status === 404 ? 'Book not found' : 'Something went wrong'}</h1>
        <p className="muted"><Link to="/books" className="text-link">Back to all books</Link></p>
      </div>
    )
  }
  // key makes the format choice start fresh when the visitor moves to another book
  return <BookDetail key={book.id} book={book} />
}

function BookDetail({ book }) {
  const { addItem } = useCart()
  const { openPanel } = usePanel()

  const [productId, setProductId] = useState(book.products[0]?.id)
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
        <Cover book={book} />
      </div>

      <div>
        <h1>{book.title}</h1>
        <p className="book-detail-author">{book.authors.join(', ')}</p>
        <Rating average={book.averageRating} count={book.reviewCount} />

        {product ? (
          <fieldset className="format-picker">
            <legend>Format</legend>
            {book.products.map((item) => (
              <label key={item.id} className={`format-option${item.id === productId ? ' is-selected' : ''}`}>
                <input
                  type="radio"
                  name="format"
                  value={item.id}
                  checked={item.id === productId}
                  onChange={() => setProductId(item.id)}
                />
                <span>{FORMAT_NAMES[item.format]}</span>
                <strong>{formatPrice(item.price, item.currency)}</strong>
              </label>
            ))}
          </fieldset>
        ) : (
          <p className="muted">This book is not available right now.</p>
        )}

        {product && (
          <div className="buy-row">
            <button type="button" className="btn" onClick={addToCart} disabled={busy || !product.inStock}>
              {product.inStock ? 'Add to cart' : 'Out of stock'}
            </button>
            <WishlistButton book={book} className="wish-btn-inline" />
          </div>
        )}
        {message && <p className="form-error" role="alert">{message}</p>}

        <p className="book-detail-description">{book.description}</p>

        <dl className="book-facts">
          {book.publisher && <><dt>Publisher</dt><dd>{book.publisher}</dd></>}
          {book.pageCount && <><dt>Pages</dt><dd>{book.pageCount}</dd></>}
          {book.categories.length > 0 && (
            <>
              <dt>Categories</dt>
              <dd>
                {book.categories.map((category, index) => (
                  <span key={category.id}>
                    {index > 0 && ', '}
                    <Link to={`/books?category=${category.slug}`} className="text-link">{category.name}</Link>
                  </span>
                ))}
              </dd>
            </>
          )}
        </dl>

        <section className="reviews" aria-labelledby="reviews-title">
          <h2 id="reviews-title">Reviews</h2>
          {book.reviews.length === 0 && <p className="muted">There are no reviews yet.</p>}
          {book.reviews.map((review, index) => (
            <article key={index} className="review">
              <Stars value={review.rating} />
              {review.title && <h3>{review.title}</h3>}
              {review.comment && <p>{review.comment}</p>}
              <p className="muted review-author">{review.authorName}</p>
            </article>
          ))}
        </section>
      </div>
    </div>
  )
}

function Rating({ average, count }) {
  if (!count) return <p className="muted rating">No ratings yet</p>
  return (
    <p className="rating">
      <Stars value={Math.round(average)} />
      <span className="muted">{Number(average).toFixed(1)} ({count} {count === 1 ? 'review' : 'reviews'})</span>
    </p>
  )
}

function Stars({ value }) {
  return (
    <span className="stars" role="img" aria-label={`${value} out of 5 stars`}>
      {[1, 2, 3, 4, 5].map((n) => (
        <Star key={n} size={16} strokeWidth={1.5} className={n <= value ? 'star-on' : 'star-off'} aria-hidden="true" />
      ))}
    </span>
  )
}
