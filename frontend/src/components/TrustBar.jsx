import { CreditCard, Download, Truck } from 'lucide-react'
import './TrustBar.css'

const ITEMS = [
  { Icon: Truck, title: 'Fast delivery', text: 'Printed books in 1-3 business days' },
  { Icon: CreditCard, title: 'Secure payments', text: 'Card payments powered by Stripe' },
  { Icon: Download, title: 'Instant e-books', text: 'Download right after payment' },
]

export default function TrustBar() {
  return (
    <section className="container section" aria-label="Why shop with Bookora">
      <ul className="trust-list">
        {ITEMS.map(({ Icon, title, text }) => (
          <li key={title} className="trust-item">
            <Icon size={28} strokeWidth={1.5} aria-hidden="true" />
            <div>
              <p className="trust-title">{title}</p>
              <p className="trust-text">{text}</p>
            </div>
          </li>
        ))}
      </ul>
    </section>
  )
}
