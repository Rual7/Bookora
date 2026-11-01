import { Link } from 'react-router-dom'
import { Compass } from 'lucide-react'
import StatusPage from '../components/StatusPage.jsx'
import { t } from '../i18n/t.js'

export default function NotFoundPage() {
  return (
    <StatusPage icon={Compass} code="404" title={t('notFound.title')} text={t('notFound.text')}>
      <Link to="/" className="btn">{t('notFound.home')}</Link>
      <Link to="/books" className="btn btn-outline">{t('common.browse')}</Link>
    </StatusPage>
  )
}
