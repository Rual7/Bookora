import { useRef } from 'react'
import { Link } from 'react-router-dom'
import { BookOpen, ChevronLeft, ChevronRight, Feather, Fingerprint, Heart, Landmark, Rocket, Sparkles, Sprout } from 'lucide-react'
import { useScrollEdges } from '../hooks/useScrollEdges.js'
import './CategoryStrip.css'

const CATEGORIES = [
  { slug: 'fiction', label: 'Fiction', Icon: BookOpen },
  { slug: 'fantasy', label: 'Fantasy', Icon: Sparkles },
  { slug: 'science-fiction', label: 'Science Fiction', Icon: Rocket },
  { slug: 'romance', label: 'Romance', Icon: Heart },
  { slug: 'philosophy', label: 'Philosophy', Icon: Landmark },
  { slug: 'self-development', label: 'Self-development', Icon: Sprout },
  { slug: 'mystery-thriller', label: 'Mystery & Thriller', Icon: Fingerprint },
  { slug: 'classics', label: 'Classics', Icon: Feather },
]

export default function CategoryStrip() {
  const listRef = useRef(null)
  const { atStart, atEnd, update, scrollByPage } = useScrollEdges(listRef)
  const scrolls = !(atStart && atEnd)

  return (
    <nav className="container category-strip" aria-label="Browse by category">
      {scrolls && (
        <button type="button" className="strip-btn strip-prev" aria-label="Previous categories"
          disabled={atStart} onClick={() => scrollByPage(-1)}>
          <ChevronLeft size={20} aria-hidden="true" />
        </button>
      )}

      <ul className="category-list" ref={listRef} onScroll={update}>
        {CATEGORIES.map(({ slug, label, Icon }) => (
          <li key={slug}>
            <Link to={`/books?category=${slug}`} className="category-item">
              <span className="category-circle">
                <Icon size={28} strokeWidth={1.5} aria-hidden="true" />
              </span>
              <span className="category-label">{label}</span>
            </Link>
          </li>
        ))}
      </ul>

      {scrolls && (
        <button type="button" className="strip-btn strip-next" aria-label="Next categories"
          disabled={atEnd} onClick={() => scrollByPage(1)}>
          <ChevronRight size={20} aria-hidden="true" />
        </button>
      )}
    </nav>
  )
}
