import { Link } from 'react-router-dom'
import Cover from './Cover.jsx'
import WishlistButton from './WishlistButton.jsx'
import { t } from '../i18n/t.js'
import { formatPrice } from '../utils/format.js'
import './BookCard.css'

const AVAILABILITY = {
  IN_STOCK: 'pill-ok',
  OUT_OF_STOCK: 'pill-muted',
  INSTANT_DOWNLOAD: 'pill-ok',
}

/** `Heading` is the heading level of the title; it must follow the heading above the list, so a page with only an h1 passes "h2". */
export default function BookCard({ book, Heading = 'h3' }) {
  const availability = book.availability in AVAILABILITY ? book.availability : 'OUT_OF_STOCK'

  return (
    <article className="book-card">
      <div className="book-card-cover">
        <Link to={`/books/${book.slug}`} tabIndex={-1} aria-hidden="true">
          <Cover book={book} />
        </Link>
        <WishlistButton book={book} className="wish-btn" />
      </div>
      <Heading className="book-card-title">
        <Link to={`/books/${book.slug}`}>{book.title}</Link>
      </Heading>
      <p className="book-card-author">{book.authors.join(', ')}</p>
      {book.price != null && <p className="book-card-price">{formatPrice(book.price)}</p>}
      <span className={`pill ${AVAILABILITY[availability]}`}>{t(`availability.${availability}`)}</span>
    </article>
  )
}
