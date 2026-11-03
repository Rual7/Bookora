import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { Download, PackageX } from 'lucide-react'
import AddressText from '../components/AddressText.jsx'
import StatusPage from '../components/StatusPage.jsx'
import OrderStatus from '../components/OrderStatus.jsx'
import { t } from '../i18n/t.js'
import { usePageMeta } from '../hooks/usePageMeta.js'
import { useOrder } from '../hooks/useOrder.js'
import { usePayment } from '../hooks/usePayment.js'
import { downloadFile } from '../api/client.js'
import { formatDate, formatPrice, isDigital } from '../utils/format.js'
import './OrderPage.css'

const CONFIRM_EVERY_MS = 2000
const GIVE_UP_AFTER_MS = 40000

// Old orders keep the currency they were placed in, so prices are shown with order.currency.
export default function OrderPage() {
  const { orderNumber } = useParams()
  usePageMeta({ title: t('order.title', { number: orderNumber }), noindex: true })
  const { state, pathname } = useLocation()
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const { order, error, loading, reload } = useOrder(orderNumber)
  const payment = usePayment()

  // Set when the customer comes back from the payment page; the order is confirmed by a webhook that may be a moment late.
  const returnedFrom = params.get('payment')
  // The checkout page passes a message when it could not open the payment page; it is shown once, then dropped from history.
  const [checkoutProblem] = useState(state?.paymentProblem ?? '')
  const [gaveUp, setGaveUp] = useState(false)
  const [downloadProblem, setDownloadProblem] = useState('')
  const confirming = returnedFrom === 'success' && order?.status === 'PENDING_PAYMENT' && !gaveUp

  useEffect(() => {
    if (checkoutProblem) navigate(pathname, { replace: true, state: null })
  }, [checkoutProblem, navigate, pathname])

  useEffect(() => {
    if (!confirming) return
    const poll = setInterval(reload, CONFIRM_EVERY_MS)
    const stop = setTimeout(() => setGaveUp(true), GIVE_UP_AFTER_MS)
    return () => {
      clearInterval(poll)
      clearTimeout(stop)
    }
  }, [confirming, reload])

  if (loading) return <div className="container page"><p className="muted">{t('common.loading')}</p></div>
  if (error) {
    const missing = error.status === 404
    return (
      <StatusPage icon={PackageX} code={missing ? '404' : undefined} title={missing ? t('order.notFound') : t('common.wrong')}
        text={missing ? t('order.notFoundText') : t('error.loadText')}>
        <Link to="/orders" className="btn">{t('order.backToOrders')}</Link>
      </StatusPage>
    )
  }
  if (!order) return null

  const price = (amount) => formatPrice(amount, order.currency)
  const unpaid = order.status === 'PENDING_PAYMENT'
  const paid = ['PAID', 'SHIPPED', 'DELIVERED'].includes(order.status)
  const notice = describeNotice()

  async function download(item) {
    setDownloadProblem('')
    try {
      await downloadFile(`/api/downloads/${item.id}`)
    } catch (problem) {
      setDownloadProblem(problem.message)
    }
  }

  // One message at a time: what just happened if there is something to say, otherwise that the order awaits payment.
  function describeNotice() {
    if (order.status === 'PAID' && returnedFrom === 'success') return { tone: 'ok', text: t('order.paidThanks') }
    if (confirming) return { tone: 'ok', text: t('order.confirming') }
    if (!unpaid) return null
    if (checkoutProblem) return { tone: 'warn', text: t('order.paymentProblem', { message: checkoutProblem }) }
    if (gaveUp) return { tone: 'warn', text: t('order.confirmingLate') }
    if (returnedFrom === 'cancelled') return { tone: 'warn', text: t('order.paymentCancelled') }
    return { tone: 'warn', text: t('order.awaiting') }
  }

  return (
    <div className="container page">
      <p><Link to="/orders" className="text-link">&larr; {t('order.allOrders')}</Link></p>
      <h1>{t('order.title', { number: order.orderNumber })}</h1>

      {notice && <p className={`order-notice order-notice-${notice.tone}`} role="status">{notice.text}</p>}

      <p className="order-meta">
        <OrderStatus status={order.status} />
        <span className="muted">
          {t('order.placedOn')} <time dateTime={order.createdAt}>{formatDate(order.createdAt)}</time>
        </span>
      </p>

      {downloadProblem && <p className="form-error" role="alert">{t('order.downloadProblem', { message: downloadProblem })}</p>}

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
                <th scope="row">
                  {item.title}
                  {paid && isDigital(item.format) && (
                    <button type="button" className="link-btn order-download" onClick={() => download(item)}
                      aria-label={t('order.downloadLabel', { title: item.title })}>
                      <Download size={16} strokeWidth={1.5} aria-hidden="true" /> {t('order.download')}
                    </button>
                  )}
                </th>
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

      <div className="order-bottom">
        <section className="order-delivery" aria-labelledby="delivery-title">
          <h2 id="delivery-title">{t('cart.delivery')}</h2>
          {order.shippingAddress ? (
            <AddressText address={order.shippingAddress} />
          ) : (
            <p className="muted">{t('order.noDelivery')}</p>
          )}
        </section>

        {unpaid && !confirming && (
          <div className="order-pay">
            <button type="button" className="btn" onClick={() => payment.pay(order.orderNumber)} disabled={payment.busy}>
              {payment.busy ? t('order.paying') : t('order.pay')}
            </button>
            {payment.error && <p className="form-error" role="alert">{payment.error}</p>}
          </div>
        )}
      </div>
    </div>
  )
}
