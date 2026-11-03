import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import AddressText from '../../components/AddressText.jsx'
import OrderStatus from '../../components/OrderStatus.jsx'
import OrderTable from '../../components/OrderTable.jsx'
import { api } from '../../api/client.js'
import { t } from '../../i18n/t.js'
import { useFetch } from '../../hooks/useFetch.js'
import { formatDate } from '../../utils/format.js'
import './Admin.css'

export default function AdminOrderPage() {
  const { orderNumber } = useParams()
  const { data, error, loading, reload } = useFetch(`/api/admin/orders/${encodeURIComponent(orderNumber)}`)
  const [problem, setProblem] = useState('')
  const [busy, setBusy] = useState(false)

  if (error) return <p className="form-error" role="alert">{error.message}</p>
  if (loading || !data) return <p className="muted">{t('admin.loading')}</p>

  const { order } = data
  const next = nextStep(order)

  async function moveTo(status) {
    setBusy(true)
    setProblem('')
    try {
      await api.put(`/api/admin/orders/${order.orderNumber}/status`, { status })
      reload()
    } catch (failure) {
      setProblem(failure.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <p><Link to="/admin/orders" className="text-link">&larr; {t('admin.orders')}</Link></p>
      <h2 className="admin-heading">{t('order.title', { number: order.orderNumber })}</h2>

      <p className="order-meta">
        <OrderStatus status={order.status} />
        <span className="muted">
          {t('order.placedOn')} <time dateTime={order.createdAt}>{formatDate(order.createdAt)}</time>
        </span>
      </p>
      <p className="admin-notice">{t('admin.customer')}: {data.customerName} ({data.customerEmail})</p>

      <OrderTable order={order} />

      <div className="order-bottom">
        <section className="order-delivery" aria-labelledby="delivery-title">
          <h3 id="delivery-title">{t('cart.delivery')}</h3>
          {order.shippingAddress ? <AddressText address={order.shippingAddress} /> : <p className="muted">{t('order.noDelivery')}</p>}
        </section>

        <div className="order-pay">
          {next ? (
            <button type="button" className="btn" disabled={busy} onClick={() => moveTo(next.status)}>{t(next.label)}</button>
          ) : (
            <p className="muted">{t('admin.noMoreSteps')}</p>
          )}
          {problem && <p className="form-error" role="alert">{problem}</p>}
        </div>
      </div>
    </>
  )
}

// Mirrors the server rule: printed orders ship first, e-book-only orders go straight to delivered.
function nextStep(order) {
  if (order.status === 'PAID') {
    return order.shippingAddress
      ? { status: 'SHIPPED', label: 'admin.markShipped' }
      : { status: 'DELIVERED', label: 'admin.markDelivered' }
  }
  if (order.status === 'SHIPPED') return { status: 'DELIVERED', label: 'admin.markDelivered' }
  return null
}
