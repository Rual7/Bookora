import { useState } from 'react'
import { Star } from 'lucide-react'
import { t } from '../i18n/t.js'
import './StarInput.css'

/** Five stars to click for a rating from 1 to 5. Radio buttons underneath, so the keyboard and screen readers work too. */
export default function StarInput({ value, onChange, error }) {
  const [hovered, setHovered] = useState(0)
  const shown = hovered || value

  return (
    <fieldset className="star-input">
      <legend className="field-label">{t('review.rating')}</legend>
      <div className="star-input-row" onMouseLeave={() => setHovered(0)}>
        {[1, 2, 3, 4, 5].map((n) => (
          <label key={n} className="star-input-star" onMouseEnter={() => setHovered(n)}>
            <input type="radio" name="rating" value={n} checked={value === n} onChange={() => onChange(n)} />
            <span className="visually-hidden">{t('book.stars', { value: n })}</span>
            <Star size={32} strokeWidth={1.5} className={n <= shown ? 'star-on' : 'star-off'} aria-hidden="true" />
          </label>
        ))}
      </div>
      {error && <p className="field-error">{error}</p>}
    </fieldset>
  )
}
