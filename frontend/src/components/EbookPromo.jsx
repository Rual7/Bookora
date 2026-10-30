import { Link } from 'react-router-dom'
import './EbookPromo.css'

export default function EbookPromo() {
  return (
    <section className="container section" aria-labelledby="ebook-title">
      <div className="ebook-promo">
        <div className="ebook-copy">
          <p className="ebook-eyebrow">E-books</p>
          <h2 id="ebook-title">Unlimited reading</h2>
          <p className="ebook-text">Take your favorite stories wherever you go.</p>
          <Link to="/books?format=ebook" className="ebook-btn">
            Discover e-books
          </Link>
        </div>
        <img className="ebook-image" src="/images/Bookora-PromoIMG.jpg" alt="" width="2228" height="464"
          loading="lazy" />
      </div>
    </section>
  )
}
