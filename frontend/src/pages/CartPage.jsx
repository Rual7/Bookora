import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Trash2 } from 'lucide-react'
import Cover from '../components/Cover.jsx'
import EmptyCart from '../components/EmptyCart.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { useCart } from '../context/CartContext.jsx'
import { t } from '../i18n/t.js'
import { usePageMeta } from '../hooks/usePageMeta.js'
import { formatPrice } from '../utils/format.js'
import './CartPage.css'

export default function CartPage() {
  usePageMeta({ title: t('meta.cart'), noindex: true })

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
    return <EmptyCart title={t('cart.title')} />
  }

  return (
    <div className="container page">
      <h1>{t('cart.title')}</h1>
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
                <p className="muted">{t(`format.${item.format}`)} &middot; {formatPrice(item.unitPrice)}</p>

                <div className="cart-actions">
                  {item.maxQuantity > 1 && (
                    <label className="qty">
                      <span className="visually-hidden">{t('cart.quantityOf', { title: item.title })}</span>
                      <select value={item.quantity}
                        onChange={(event) => run(() => changeQuantity(item.productId, Number(event.target.value)))}>
                        {Array.from({ length: item.maxQuantity }, (_, i) => i + 1).map((n) => (
                          <option key={n} value={n}>{n}</option>
                        ))}
                      </select>
                    </label>
                  )}
                  <button type="button" className="link-btn" onClick={() => run(() => removeItem(item.productId))}>
                    <Trash2 size={16} strokeWidth={1.5} aria-hidden="true" /> {t('cart.remove')}
                  </button>
                </div>
              </div>

              <p className="cart-line-total">{formatPrice(item.lineTotal)}</p>
            </li>
          ))}
        </ul>

        <aside className="cart-summary" aria-label={t('cart.summary')}>
          <h2>{t('checkout.summary')}</h2>
          <p className="summary-row">
            <span>{t('cart.subtotalCount', { count: cart.itemCount })}</span>
            <strong>{formatPrice(cart.subtotal)}</strong>
          </p>
          <p className="summary-row">
            <span>{t('cart.delivery')}</span>
            <span>{Number(cart.shippingCost) === 0 ? t('shipping.free') : formatPrice(cart.shippingCost)}</span>
          </p>
          <p className="summary-row summary-total">
            <span>{t('cart.total')}</span>
            <strong>{formatPrice(cart.total)}</strong>
          </p>
          <p className="muted summary-note">{t('cart.deliveryNote', { cost: formatPrice(cart.standardShipping), limit: formatPrice(cart.freeShippingAbove) })}</p>
          {user ? (
            <Link to="/checkout" className="btn btn-block">{t('cart.checkout')}</Link>
          ) : (
            <>
              <Link to="/login" className="btn btn-block">{t('cart.signInToCheckout')}</Link>
              <p className="muted summary-note">{t('cart.keptNote')}</p>
            </>
          )}
        </aside>
      </div>
    </div>
  )
}
