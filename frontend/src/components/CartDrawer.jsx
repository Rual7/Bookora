import { Link, useNavigate } from 'react-router-dom'
import { Trash2 } from 'lucide-react'
import Drawer from './Drawer.jsx'
import Cover from './Cover.jsx'
import { useCart } from '../context/CartContext.jsx'
import { t } from '../i18n/t.js'
import { formatPrice } from '../utils/format.js'
import { usePanel } from '../context/PanelContext.jsx'

export default function CartDrawer() {
  const { panel, closePanel } = usePanel()
  const { cart, changeQuantity, removeItem } = useCart()
  const navigate = useNavigate()

  function goToCart() {
    closePanel()
    navigate('/cart')
  }

  return (
    <Drawer open={panel === 'cart'} title={t('cart.title')} onClose={closePanel}>
      {cart.items.length === 0 ? (
        <p className="drawer-empty">{t('cart.empty')}</p>
      ) : (
        <>
          <ul className="drawer-body">
            {cart.items.map((item) => (
              <li key={item.productId} className="drawer-row">
                <Cover book={{ id: item.bookId, title: item.title, authors: item.authors, coverImagePath: item.coverImagePath }} />
                <div>
                  <Link to={`/books/${item.bookSlug}`} className="drawer-row-title" onClick={closePanel}>
                    {item.title}
                  </Link>
                  <p>{t(`format.${item.format}`)}</p>
                  <div className="drawer-row-actions">
                    {item.maxQuantity > 1 && (
                      <label>
                        <span className="visually-hidden">{t('cart.quantityOf', { title: item.title })}</span>
                        <select className="qty-select" value={item.quantity}
                          onChange={(event) => changeQuantity(item.productId, Number(event.target.value))}>
                          {Array.from({ length: item.maxQuantity }, (_, i) => i + 1).map((n) => (
                            <option key={n} value={n}>{n}</option>
                          ))}
                        </select>
                      </label>
                    )}
                    <button type="button" className="link-btn" onClick={() => removeItem(item.productId)}>
                      <Trash2 size={16} strokeWidth={1.5} aria-hidden="true" /> {t('cart.remove')}
                    </button>
                  </div>
                </div>
                <p className="drawer-row-price">{formatPrice(item.lineTotal)}</p>
              </li>
            ))}
          </ul>
          <div className="drawer-foot">
            <p className="drawer-total">
              <span>{t('cart.subtotal')}</span>
              <span>{formatPrice(cart.subtotal)}</span>
            </p>
            <button type="button" className="btn btn-block" onClick={goToCart}>{t('cart.goToCheckout')}</button>
          </div>
        </>
      )}
    </Drawer>
  )
}
