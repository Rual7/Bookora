import { Heart } from 'lucide-react'
import { useWishlist } from '../context/WishlistContext.jsx'

/** Heart button. `book` needs at least id and title. Works for visitors too. */
export default function WishlistButton({ book, className }) {
  const wishlist = useWishlist()
  const wished = wishlist.has(book.id)

  async function onClick() {
    try {
      await wishlist.toggle(book)
    } catch {
      // The heart simply stays as it was; the next click tries again.
    }
  }

  return (
    <button
      type="button"
      className={`${className}${wished ? ' is-active' : ''}`}
      aria-label={wished ? `Remove ${book.title} from wishlist` : `Add ${book.title} to wishlist`}
      aria-pressed={wished}
      onClick={onClick}
    >
      <Heart size={18} strokeWidth={1.75} aria-hidden="true" />
    </button>
  )
}
