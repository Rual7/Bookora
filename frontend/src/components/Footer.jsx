import { Link } from 'react-router-dom'
import Logo from './Logo.jsx'
import { t } from '../i18n/t.js'
import './Footer.css'

const YEAR = new Date().getFullYear()

export default function Footer() {
  return (
    <footer className="site-footer">
      <div className="container footer-inner">
        <div className="footer-brand">
          <Logo />
          <p className="muted">{t('footer.tagline')}</p>
        </div>

        <nav className="footer-col" aria-label={t('footer.shop')}>
          <h2>{t('footer.shop')}</h2>
          <Link to="/books">{t('footer.allBooks')}</Link>
          <Link to="/books?format=ebook">{t('nav.ebooks')}</Link>
          <Link to="/categories">{t('nav.categories')}</Link>
        </nav>

        <nav className="footer-col" aria-label={t('footer.account')}>
          <h2>{t('footer.account')}</h2>
          <Link to="/login">{t('nav.signIn')}</Link>
          <Link to="/cart">{t('footer.cart')}</Link>
          <Link to="/orders">{t('footer.orders')}</Link>
          <Link to="/addresses">{t('footer.addresses')}</Link>
        </nav>

        <nav className="footer-col" aria-label={t('footer.company')}>
          <h2>{t('footer.company')}</h2>
          <Link to="/about">{t('footer.about')}</Link>
        </nav>
      </div>

      <div className="container footer-bottom">
        <p className="muted">&copy; {YEAR} Bookora</p>
      </div>
    </footer>
  )
}
