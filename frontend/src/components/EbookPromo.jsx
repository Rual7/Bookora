import { Link } from 'react-router-dom'
import { t } from '../i18n/t.js'
import './EbookPromo.css'

export default function EbookPromo() {
  return (
    <section className="container section" aria-labelledby="ebook-title">
      <div className="ebook-promo">
        <div className="ebook-copy">
          <p className="ebook-eyebrow">{t('ebookPromo.eyebrow')}</p>
          <h2 id="ebook-title">{t('ebookPromo.title')}</h2>
          <p className="ebook-text">{t('ebookPromo.text')}</p>
          <Link to="/books?format=ebook" className="ebook-btn">
            {t('ebookPromo.cta')}
          </Link>
        </div>
        <img className="ebook-image" src="/images/Bookora-PromoIMG.jpg" alt="" width="2228" height="464"
          loading="lazy" />
      </div>
    </section>
  )
}
