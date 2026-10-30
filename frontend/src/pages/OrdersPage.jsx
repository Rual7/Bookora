import { Link } from 'react-router-dom'
import AccountNav from '../components/AccountNav.jsx'
import OrderStatus from '../components/OrderStatus.jsx'
import { t } from '../i18n/t.js'
import { useFetch } from '../hooks/useFetch.js'
import { formatDate, formatPrice } from '../utils/format.js'

export default function OrdersPage() {
  const { data: orders, error, loading } = useFetch('/api/orders')

  return (
    <div className="container page">
      <h1>{t('nav.account')}</h1>
      <AccountNav />

      {error && <p className="form-error" role="alert">{error.message}</p>}
      {loading && <p className="muted">{t('common.loading')}</p>}
      {orders?.length === 0 && (
        <>
          <p className="muted">{t('orders.none')}</p>
          <p><Link to="/books" className="btn">{t('common.browse')}</Link></p>
        </>
      )}

      {orders?.length > 0 && (
        <div className="table-wrap">
          <table className="data-table">
            <caption className="visually-hidden">{t('orders.caption')}</caption>
            <thead>
              <tr>
                <th scope="col">{t('orders.order')}</th>
                <th scope="col">{t('orders.date')}</th>
                <th scope="col" className="num">{t('orders.items')}</th>
                <th scope="col">{t('orders.status')}</th>
                <th scope="col" className="num">{t('orders.total')}</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.orderNumber}>
                  <th scope="row">
                    <Link to={`/orders/${order.orderNumber}`} className="text-link">{order.orderNumber}</Link>
                  </th>
                  <td data-label={t('orders.date')}>
                    <time dateTime={order.createdAt}>{formatDate(order.createdAt)}</time>
                  </td>
                  <td className="num" data-label={t('orders.items')}>{order.itemCount}</td>
                  <td data-label={t('orders.status')}><OrderStatus status={order.status} /></td>
                  <td className="num" data-label={t('orders.total')}>{formatPrice(order.total, order.currency)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
