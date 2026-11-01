import { useState } from 'react'
import Pager from '../../components/Pager.jsx'
import { api } from '../../api/client.js'
import { t } from '../../i18n/t.js'
import { useFetch } from '../../hooks/useFetch.js'
import { formatDate } from '../../utils/format.js'
import './Admin.css'

export default function AdminCustomersPage() {
  const [page, setPage] = useState(0)
  const [problem, setProblem] = useState('')
  const { data, error, loading, reload } = useFetch(`/api/admin/customers?page=${page}`)

  async function setEnabled(id, enabled) {
    setProblem('')
    try {
      await api.put(`/api/admin/customers/${id}/enabled`, { enabled })
      reload()
    } catch (failure) {
      setProblem(failure.message)
    }
  }

  return (
    <>
      {(error || problem) && <p className="form-error" role="alert">{problem || error.message}</p>}
      {loading && <p className="muted">{t('admin.loading')}</p>}

      {data?.content.length > 0 && (
        <div className="table-wrap">
          <table className="data-table admin-table">
            <caption className="visually-hidden">{t('admin.customersCaption')}</caption>
            <thead>
              <tr>
                <th scope="col">{t('admin.col.name')}</th>
                <th scope="col">{t('admin.col.email')}</th>
                <th scope="col">{t('admin.col.role')}</th>
                <th scope="col" className="num">{t('admin.col.orders')}</th>
                <th scope="col">{t('admin.col.joined')}</th>
                <th scope="col">{t('admin.col.status')}</th>
                <th scope="col" className="actions"><span className="visually-hidden">{t('admin.edit')}</span></th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((customer) => (
                <tr key={customer.id}>
                  <th scope="row">{customer.name}</th>
                  <td data-label={t('admin.col.email')}>{customer.email}</td>
                  <td data-label={t('admin.col.role')}>
                    {customer.role === 'ADMIN' ? t('admin.adminAccount') : t('admin.customerAccount')}
                  </td>
                  <td className="num" data-label={t('admin.col.orders')}>{customer.orderCount}</td>
                  <td data-label={t('admin.col.joined')}>
                    <time dateTime={customer.createdAt}>{formatDate(customer.createdAt)}</time>
                  </td>
                  <td data-label={t('admin.col.status')}>
                    <span className={`status ${customer.enabled ? 'status-clean' : 'status-cancelled'}`}>
                      {customer.enabled ? t('admin.accountOn') : t('admin.accountOff')}
                    </span>
                  </td>
                  <td className="actions">
                    {customer.role !== 'ADMIN' && (
                      <div className="admin-actions">
                        <button type="button" className="btn btn-small btn-outline" onClick={() => setEnabled(customer.id, !customer.enabled)}>
                          {customer.enabled ? t('admin.disable') : t('admin.enable')}
                        </button>
                      </div>
                    )}
                  </td>
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
