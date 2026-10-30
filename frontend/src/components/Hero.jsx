import { Link } from 'react-router-dom'
import { ArrowRight } from 'lucide-react'
import { t } from '../i18n/t.js'
import './Hero.css'

export default function Hero() {
  return (
    <section className="hero" aria-labelledby="hero-title">
      <div className="container hero-inner">
        <div className="hero-copy">
          <p className="hero-eyebrow">{t('hero.eyebrow')}</p>
          <h1 id="hero-title">
            {t('hero.title1')}
            <br />
            {t('hero.title2')}
          </h1>
          <p className="hero-lead">{t('hero.lead')}</p>
          <Link to="/books" className="btn btn-block">
            {t('hero.cta')}
            <ArrowRight className="btn-arrow" size={18} strokeWidth={2} aria-hidden="true" />
          </Link>
        </div>
      </div>
    </section>
  )
}
