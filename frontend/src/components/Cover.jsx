import { t } from '../i18n/t.js'
import './Cover.css'

/** The book's cover image, or a typographic cover in the brand palette when the book has no picture yet. */
export default function Cover({ book }) {
  const label = t('book.cover', { title: book.title })
  if (book.coverImagePath) {
    return (
      <img
        className="cover cover-image"
        src={book.coverImagePath}
        alt={label}
        width="300"
        height="450"
        loading="lazy"
      />
    )
  }
  const variant = Number(book.id) % 4
  return (
    <div className={`cover cover-generated cover-v${variant}`} role="img" aria-label={label}>
      <span className="cover-rule" aria-hidden="true" />
      <span className="cover-title" aria-hidden="true">
        {book.title}
      </span>
      <span className="cover-author" aria-hidden="true">
        {book.authors?.[0]}
      </span>
    </div>
  )
}
