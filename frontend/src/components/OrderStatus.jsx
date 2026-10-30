import { t } from '../i18n/t.js'

export default function OrderStatus({ status }) {
  return <span className={`status status-${status.toLowerCase()}`}>{t(`status.${status}`)}</span>
}
