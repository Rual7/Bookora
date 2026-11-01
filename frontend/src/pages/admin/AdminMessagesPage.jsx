import { useState } from 'react'
import Pager from '../../components/Pager.jsx'
import { api } from '../../api/client.js'
import { t } from '../../i18n/t.js'
import { useFetch } from '../../hooks/useFetch.js'
import { formatDate } from '../../utils/format.js'
import './Admin.css'

export default function AdminMessagesPage() {
  const [page, setPage] = useState(0)
  const [problem, setProblem] = useState('')
  const { data, error, loading, reload } = useFetch(`/api/admin/messages?page=${page}`)

  async function setHandled(id, handled) {
    setProblem('')
    try {
      await api.put(`/api/admin/messages/${id}/handled`, { handled })
      reload()
    } catch (failure) {
      setProblem(failure.message)
    }
  }

  return (
    <>
      {(error || problem) && <p className="form-error" role="alert">{problem || error.message}</p>}
      {loading && <p className="muted">{t('admin.loading')}</p>}
      {data?.content.length === 0 && <p className="muted">{t('admin.empty')}</p>}

      {data?.content.length > 0 && (
        <div className="table-wrap">
          <table className="data-table admin-table">
            <caption className="visually-hidden">{t('admin.messagesCaption')}</caption>
            <thead>
              <tr>
                <th scope="col">{t('admin.col.name')}</th>
                <th scope="col">{t('admin.col.message')}</th>
                <th scope="col">{t('admin.col.date')}</th>
                <th scope="col">{t('admin.col.status')}</th>
                <th scope="col" className="actions"><span className="visually-hidden">{t('admin.edit')}</span></th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((message) => (
                <tr key={message.id}>
                  <th scope="row">
                    {message.name}
                    <br />
                    <a href={`mailto:${message.email}`} className="text-link">{message.email}</a>
                  </th>
                  <td className="admin-message" data-label={t('admin.col.message')}>{message.message}</td>
                  <td data-label={t('admin.col.date')}>
                    <time dateTime={message.createdAt}>{formatDate(message.createdAt)}</time>
                  </td>
                  <td data-label={t('admin.col.status')}>
                    <span className={`status ${message.handled ? 'status-clean' : 'status-pending'}`}>
                      {message.handled ? t('admin.handled') : t('admin.open')}
                    </span>
                  </td>
                  <td className="actions">
                    <div className="admin-actions">
                      <button type="button" className="btn btn-small btn-outline" onClick={() => setHandled(message.id, !message.handled)}>
                        {message.handled ? t('admin.markOpen') : t('admin.markHandled')}
                      </button>
                    </div>
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
