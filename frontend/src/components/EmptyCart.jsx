import { Link } from 'react-router-dom'
import { ShoppingCart } from 'lucide-react'
import EmptyState from './EmptyState.jsx'
import { t } from '../i18n/t.js'

/** Shown by the cart and checkout pages when there is nothing in the cart. */
export default function EmptyCart({ title }) {
  return (
    <div className="container page">
      <h1>{title}</h1>
      <EmptyState icon={ShoppingCart} text={t('cart.empty')}>
        <Link to="/books" className="btn">{t('common.browse')}</Link>
      </EmptyState>
    </div>
  )
}
