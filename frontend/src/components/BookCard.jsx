import { Link } from 'react-router-dom'
import Cover from './Cover.jsx'
import WishlistButton from './WishlistButton.jsx'
import { formatPrice } from '../utils/format.js'
import './BookCard.css'

const AVAILABILITY = {
  IN_STOCK: { label: 'In stock', className: 'pill-ok' },
  OUT_OF_STOCK: { label: 'Out of stock', className: 'pill-muted' },
  INSTANT_DOWNLOAD: { label: 'Instant download', className: 'pill-ok' },
}

export default function BookCard({ book }) {
  const availability = AVAILABILITY[book.availability] ?? AVAILABILITY.OUT_OF_STOCK

  return (
    <article className="book-card">
      <div className="book-card-cover">
        <Link to={`/books/${book.slug}`} tabIndex={-1} aria-hidden="true">
          <Cover book={book} />
        </Link>
        <WishlistButton book={book} className="wish-btn" />
      </div>
      <h3 className="book-card-title">
        <Link to={`/books/${book.slug}`}>{book.title}</Link>
      </h3>
      <p className="book-card-author">{book.authors.join(', ')}</p>
      {book.price != null && <p className="book-card-price">{formatPrice(book.price, book.currency)}</p>}
      <span className={`pill ${availability.className}`}>{availability.label}</span>
    </article>
  )
}
