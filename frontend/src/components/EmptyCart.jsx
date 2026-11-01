import { Link } from 'react-router-dom'
import { ShoppingCart } from 'lucide-react'
import { t } from '../i18n/t.js'
import './EmptyCart.css'

/** Shown by the cart and checkout pages when there is nothing in the cart. */
export default function EmptyCart({ title }) {
  return (
    <div className="container page">
      <h1>{title}</h1>
      <div className="empty-cart">
        <span className="empty-cart-icon">
          <ShoppingCart size={32} strokeWidth={1.5} aria-hidden="true" />
        </span>
        <p>{t('cart.empty')}</p>
        <Link to="/books" className="btn">{t('common.browse')}</Link>
      </div>
    </div>
  )
}
