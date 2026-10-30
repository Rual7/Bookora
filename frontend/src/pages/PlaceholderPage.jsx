import { t } from '../i18n/t.js'

export default function PlaceholderPage({ title }) {
  return (
    <div className="container page">
      <h1>{t(title)}</h1>
      <p className="muted">{t('common.comingSoon')}</p>
    </div>
  )
}
