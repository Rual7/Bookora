import { Link } from 'react-router-dom'
import { Heart } from 'lucide-react'
import Drawer from './Drawer.jsx'
import Cover from './Cover.jsx'
import { formatPrice } from '../utils/format.js'
import { usePanel } from '../context/PanelContext.jsx'
import { useWishlist } from '../context/WishlistContext.jsx'

export default function WishlistDrawer() {
  const { panel, closePanel } = usePanel()
  const { books, toggle } = useWishlist()

  return (
    <Drawer open={panel === 'wishlist'} title="Your wishlist" onClose={closePanel}>
      {books.length === 0 ? (
        <p className="drawer-empty">Tap the heart on a book to save it here.</p>
      ) : (
        <ul className="drawer-body">
          {books.map((book) => (
            <li key={book.id} className="drawer-row">
              <Cover book={book} />
              <div>
                <Link to={`/books/${book.slug}`} className="drawer-row-title" onClick={closePanel}>
                  {book.title}
                </Link>
                <p>{book.authors.join(', ')}</p>
                {book.price != null && <p className="drawer-row-price">{formatPrice(book.price, book.currency)}</p>}
              </div>
              <button type="button" className="icon-btn" aria-label={`Remove ${book.title} from wishlist`}
                onClick={() => toggle(book)}>
                <Heart size={20} strokeWidth={1.5} className="heart-on" aria-hidden="true" />
              </button>
            </li>
          ))}
        </ul>
      )}
    </Drawer>
  )
}
