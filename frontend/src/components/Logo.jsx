import { Link } from 'react-router-dom'
import './Logo.css'

export function LogoMark({ className }) {
  return (
    <svg className={className} viewBox="0 0 100 100" aria-hidden="true" focusable="false">
      <path className="logo-pages" d="M0 27.4C25 33 42 47 47.5 63V100C32 88 14 80 0 72.6Z" />
      <path className="logo-pages" d="M13.2 16.9C30 22 46 36 47.7 56V64C40 52 28 38 13.2 26.3Z" />
      <path className="logo-gold" d="M51.6 90.9V55C53 38 70 24 86.6 18.4V62.9C72 68 58 78 51.6 90.9Z" />
      <path className="logo-pages" d="M90.6 32.8L100 29V72.7C80 78 62 90 51.6 100C53 92 70 78 90.6 65.8Z" />
      <path className="logo-gold" d="M50 0Q52 12 64 15Q52 18 50 30Q48 18 35.8 15Q48 12 50 0Z" />
    </svg>
  )
}

export default function Logo() {
  return (
    <Link to="/" className="logo" aria-label="Bookora home">
      <LogoMark className="logo-mark" />
      <span className="logo-word">Bookora</span>
    </Link>
  )
}
