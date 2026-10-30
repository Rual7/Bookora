import { CreditCard, Download, Truck } from 'lucide-react'
import { t } from '../i18n/t.js'
import './TrustBar.css'

const ITEMS = [
  { Icon: Truck, name: 'delivery' },
  { Icon: CreditCard, name: 'payments' },
  { Icon: Download, name: 'ebooks' },
]

export default function TrustBar() {
  return (
    <section className="container section" aria-label={t('trust.label')}>
      <ul className="trust-list">
        {ITEMS.map(({ Icon, name }) => (
          <li key={name} className="trust-item">
            <Icon size={28} strokeWidth={1.5} aria-hidden="true" />
            <div>
              <p className="trust-title">{t(`trust.${name}.title`)}</p>
              <p className="trust-text">{t(`trust.${name}.text`)}</p>
            </div>
          </li>
        ))}
      </ul>
    </section>
  )
}
