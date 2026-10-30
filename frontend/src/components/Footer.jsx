import { Link } from 'react-router-dom'
import Logo from './Logo.jsx'
import './Footer.css'

const YEAR = new Date().getFullYear()

export default function Footer() {
  return (
    <footer className="site-footer">
      <div className="container footer-inner">
        <div className="footer-brand">
          <Logo />
          <p className="muted">Printed books and e-books for every reader.</p>
        </div>

        <nav className="footer-col" aria-label="Shop">
          <h2>Shop</h2>
          <Link to="/books">All books</Link>
          <Link to="/books?format=ebook">E-books</Link>
          <Link to="/categories">Categories</Link>
        </nav>

        <nav className="footer-col" aria-label="Account">
          <h2>Account</h2>
          <Link to="/login">Sign in</Link>
          <Link to="/cart">Cart</Link>
          <Link to="/orders">Orders</Link>
        </nav>

        <nav className="footer-col" aria-label="Company">
          <h2>Company</h2>
          <Link to="/about">About and contact</Link>
        </nav>
      </div>

      <div className="container footer-bottom">
        <p className="muted">&copy; {YEAR} Bookora</p>
      </div>
    </footer>
  )
}
