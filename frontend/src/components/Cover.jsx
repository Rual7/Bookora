import './Cover.css'

/** Typographic placeholder cover in the brand palette; used until a book has a real cover image. */
export default function Cover({ book }) {
  if (book.coverImagePath) {
    return (
      <img
        className="cover cover-image"
        src={book.coverImagePath}
        alt={`Cover of ${book.title}`}
        width="300"
        height="450"
        loading="lazy"
      />
    )
  }
  const variant = Number(book.id) % 4
  return (
    <div className={`cover cover-generated cover-v${variant}`} role="img" aria-label={`Cover of ${book.title}`}>
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
