import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Trash2 } from 'lucide-react'
import Cover from '../components/Cover.jsx'
import { FORMAT_NAMES, formatPrice } from '../utils/format.js'
import { useAuth } from '../context/AuthContext.jsx'
import { useCart } from '../context/CartContext.jsx'
import './CartPage.css'

export default function CartPage() {
  const { user } = useAuth()
  const { cart, changeQuantity, removeItem } = useCart()
  const [error, setError] = useState('')

  // Runs a cart change and shows the server's message if it is refused.
  async function run(action) {
    setError('')
    try {
      await action()
    } catch (problem) {
      setError(problem.message)
    }
  }

  if (cart.items.length === 0) {
    return (
      <div className="container page">
        <h1>Your cart</h1>
        <p className="muted">Your cart is empty.</p>
        <p><Link to="/books" className="btn">Browse books</Link></p>
      </div>
    )
  }

  return (
    <div className="container page">
      <h1>Your cart</h1>
      {error && <p className="form-error" role="alert">{error}</p>}

      <div className="cart-layout">
        <ul className="cart-list">
          {cart.items.map((item) => (
            <li key={item.productId} className="cart-item">
              <Link to={`/books/${item.bookSlug}`} className="cart-cover" aria-hidden="true" tabIndex={-1}>
                <Cover book={{ id: item.bookId, title: item.title, authors: item.authors, coverImagePath: item.coverImagePath }} />
              </Link>

              <div className="cart-info">
                <Link to={`/books/${item.bookSlug}`} className="cart-title">{item.title}</Link>
                <p className="muted">{item.authors.join(', ')}</p>
                <p className="muted">{FORMAT_NAMES[item.format]} &middot; {formatPrice(item.unitPrice)}</p>

                <div className="cart-actions">
                  {item.maxQuantity > 1 && (
                    <label className="qty">
                      <span className="visually-hidden">Quantity of {item.title}</span>
                      <select value={item.quantity}
                        onChange={(event) => run(() => changeQuantity(item.productId, Number(event.target.value)))}>
                        {Array.from({ length: item.maxQuantity }, (_, i) => i + 1).map((n) => (
                          <option key={n} value={n}>{n}</option>
                        ))}
                      </select>
                    </label>
                  )}
                  <button type="button" className="link-btn" onClick={() => run(() => removeItem(item.productId))}>
                    <Trash2 size={16} strokeWidth={1.5} aria-hidden="true" /> Remove
                  </button>
                </div>
              </div>

              <p className="cart-line-total">{formatPrice(item.lineTotal)}</p>
            </li>
          ))}
        </ul>

        <aside className="cart-summary" aria-label="Order summary">
          <h2>Summary</h2>
          <p className="summary-row">
            <span>Subtotal ({cart.itemCount} {cart.itemCount === 1 ? 'item' : 'items'})</span>
            <strong>{formatPrice(cart.subtotal, cart.currency)}</strong>
          </p>
          <p className="muted summary-note">Delivery is calculated at checkout.</p>
          {user ? (
            <>
              <button type="button" className="btn btn-block" disabled>Checkout</button>
              <p className="muted summary-note">Online payment is coming soon.</p>
            </>
          ) : (
            <>
              <Link to="/login" state={{ from: '/cart' }} className="btn btn-block">Sign in to check out</Link>
              <p className="muted summary-note">Your cart is kept when you sign in.</p>
            </>
          )}
        </aside>
      </div>
    </div>
  )
}
