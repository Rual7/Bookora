import { Link } from 'react-router-dom'
import { ArrowRight } from 'lucide-react'
import './Hero.css'

export default function Hero() {
  return (
    <section className="hero" aria-labelledby="hero-title">
      <div className="container hero-inner">
        <div className="hero-copy">
          <p className="hero-eyebrow">Books for every story of your life</p>
          <h1 id="hero-title">
            Discover new worlds,
            <br />
            on every page
          </h1>
          <p className="hero-lead">
            Printed books and e-books from every genre, for the readers of today and tomorrow.
          </p>
          <Link to="/books" className="btn btn-block">
            Explore the catalog
            <ArrowRight className="btn-arrow" size={18} strokeWidth={2} aria-hidden="true" />
          </Link>
        </div>
      </div>
    </section>
  )
}
