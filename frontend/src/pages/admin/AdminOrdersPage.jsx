import { useState } from 'react'
import { Link } from 'react-router-dom'
import OrderStatus from '../../components/OrderStatus.jsx'
import Pager from '../../components/Pager.jsx'
import { t } from '../../i18n/t.js'
import { useFetch } from '../../hooks/useFetch.js'
import { formatDate, formatPrice } from '../../utils/format.js'
import './Admin.css'

const STATUSES = ['PENDING_PAYMENT', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED']

export default function AdminOrdersPage() {
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(0)
  const { data, error, loading } = useFetch(`/api/admin/orders?page=${page}${status ? `&status=${status}` : ''}`)

  function filter(event) {
    setStatus(event.target.value)
    setPage(0)
  }

  return (
    <>
      <div className="admin-toolbar">
        <label className="filter">
          <span className="filter-label">{t('admin.col.status')}</span>
          <select value={status} onChange={filter}>
            <option value="">{t('admin.allStatuses')}</option>
            {STATUSES.map((item) => <option key={item} value={item}>{t(`status.${item}`)}</option>)}
          </select>
        </label>
      </div>

      {error && <p className="form-error" role="alert">{error.message}</p>}
      {loading && <p className="muted">{t('admin.loading')}</p>}
      {data?.content.length === 0 && <p className="muted">{t('admin.empty')}</p>}

      {data?.content.length > 0 && (
        <div className="table-wrap">
          <table className="data-table admin-table">
            <caption className="visually-hidden">{t('admin.ordersCaption')}</caption>
            <thead>
              <tr>
                <th scope="col">{t('admin.col.order')}</th>
                <th scope="col">{t('admin.col.customer')}</th>
                <th scope="col">{t('admin.col.date')}</th>
                <th scope="col" className="num">{t('admin.col.items')}</th>
                <th scope="col">{t('admin.col.status')}</th>
                <th scope="col" className="num">{t('admin.col.total')}</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((order) => (
                <tr key={order.orderNumber}>
                  <th scope="row">
                    <Link to={`/admin/orders/${order.orderNumber}`} className="text-link">{order.orderNumber}</Link>
                  </th>
                  <td data-label={t('admin.col.customer')}>{order.customerName}</td>
                  <td data-label={t('admin.col.date')}>
                    <time dateTime={order.createdAt}>{formatDate(order.createdAt)}</time>
                  </td>
                  <td className="num" data-label={t('admin.col.items')}>{order.itemCount}</td>
                  <td data-label={t('admin.col.status')}><OrderStatus status={order.status} /></td>
                  <td className="num" data-label={t('admin.col.total')}>{formatPrice(order.total, order.currency)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {data && <Pager page={page} totalPages={data.totalPages} onChange={setPage} />}
    </>
  )
}
