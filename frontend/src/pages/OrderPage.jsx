import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import AddressText from '../components/AddressText.jsx'
import OrderStatus from '../components/OrderStatus.jsx'
import { t } from '../i18n/t.js'
import { useFetch } from '../hooks/useFetch.js'
import { formatDate, formatPrice } from '../utils/format.js'
import './OrderPage.css'

// Old orders keep the currency they were placed in, so prices are shown with order.currency.
export default function OrderPage() {
  const { orderNumber } = useParams()
  const { state, pathname } = useLocation()
  const navigate = useNavigate()
  // The notice belongs to the moment the order is placed, so it is remembered once and then dropped from history.
  const [justPlaced] = useState(Boolean(state?.placed))
  const { data: order, error, loading } = useFetch(`/api/orders/${orderNumber}`)

  useEffect(() => {
    if (justPlaced) navigate(pathname, { replace: true, state: null })
  }, [justPlaced, navigate, pathname])

  if (loading) return <div className="container page"><p className="muted">{t('common.loading')}</p></div>
  if (error) {
    return (
      <div className="container page">
        <h1>{t('order.notFound')}</h1>
        <p className="muted">{error.status === 404 ? t('order.notFoundText') : error.message}</p>
        <p><Link to="/orders" className="btn">{t('order.backToOrders')}</Link></p>
      </div>
    )
  }
  if (!order) return null

  const price = (amount) => formatPrice(amount, order.currency)

  return (
    <div className="container page">
      <p><Link to="/orders" className="text-link">&larr; {t('order.allOrders')}</Link></p>
      <h1>{t('order.title', { number: order.orderNumber })}</h1>

      {justPlaced && (
        <p className="order-placed" role="status">{t('order.placed')}</p>
      )}

      <p className="order-meta">
        <OrderStatus status={order.status} />
        <span className="muted">
          {t('order.placedOn')} <time dateTime={order.createdAt}>{formatDate(order.createdAt)}</time>
        </span>
      </p>

      <div className="table-wrap">
        <table className="data-table">
          <caption className="visually-hidden">{t('order.caption', { number: order.orderNumber })}</caption>
          <thead>
            <tr>
              <th scope="col">{t('order.book')}</th>
              <th scope="col">{t('order.format')}</th>
              <th scope="col" className="num">{t('order.price')}</th>
              <th scope="col" className="num">{t('order.qty')}</th>
              <th scope="col" className="num">{t('orders.total')}</th>
            </tr>
          </thead>
          <tbody>
            {order.items.map((item) => (
              <tr key={`${item.title}-${item.format}`}>
                <th scope="row">{item.title}</th>
                <td data-label={t('order.format')}>{t(`format.${item.format}`)}</td>
                <td className="num" data-label={t('order.price')}>{price(item.unitPrice)}</td>
                <td className="num" data-label={t('order.qty')}>{item.quantity}</td>
                <td className="num" data-label={t('orders.total')}>{price(item.lineTotal)}</td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr>
              <th scope="row" colSpan={4}>{t('cart.delivery')}</th>
              <td className="num">{Number(order.shippingCost) === 0 ? t('shipping.free') : price(order.shippingCost)}</td>
            </tr>
            <tr>
              <th scope="row" colSpan={4}>{t('orders.total')}</th>
              <td className="num"><strong>{price(order.total)}</strong></td>
            </tr>
          </tfoot>
        </table>
      </div>

      <section className="order-delivery" aria-labelledby="delivery-title">
        <h2 id="delivery-title">{t('cart.delivery')}</h2>
        {order.shippingAddress ? (
          <AddressText address={order.shippingAddress} />
        ) : (
          <p className="muted">{t('order.noDelivery')}</p>
        )}
      </section>
    </div>
  )
}
