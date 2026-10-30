import { useRef } from 'react'
import { Link } from 'react-router-dom'
import { BookOpen, ChevronLeft, ChevronRight, Feather, Fingerprint, Heart, Landmark, Rocket, Sparkles, Sprout } from 'lucide-react'
import { t } from '../i18n/t.js'
import { useScrollEdges } from '../hooks/useScrollEdges.js'
import './CategoryStrip.css'

const CATEGORIES = [
  { slug: 'fiction', Icon: BookOpen },
  { slug: 'fantasy', Icon: Sparkles },
  { slug: 'science-fiction', Icon: Rocket },
  { slug: 'romance', Icon: Heart },
  { slug: 'philosophy', Icon: Landmark },
  { slug: 'self-development', Icon: Sprout },
  { slug: 'mystery-thriller', Icon: Fingerprint },
  { slug: 'classics', Icon: Feather },
]

export default function CategoryStrip() {
  const listRef = useRef(null)
  const { atStart, atEnd, update, scrollByPage } = useScrollEdges(listRef)
  const scrolls = !(atStart && atEnd)

  return (
    <nav className="container category-strip" aria-label={t('strip.label')}>
      {scrolls && (
        <button type="button" className="strip-btn strip-prev" aria-label={t('strip.prev')}
          disabled={atStart} onClick={() => scrollByPage(-1)}>
          <ChevronLeft size={20} aria-hidden="true" />
        </button>
      )}

      <ul className="category-list" ref={listRef} onScroll={update}>
        {CATEGORIES.map(({ slug, Icon }) => (
          <li key={slug}>
            <Link to={`/books?category=${slug}`} className="category-item">
              <span className="category-circle">
                <Icon size={28} strokeWidth={1.5} aria-hidden="true" />
              </span>
              <span className="category-label">{t(`category.${slug}`)}</span>
            </Link>
          </li>
        ))}
      </ul>

      {scrolls && (
        <button type="button" className="strip-btn strip-next" aria-label={t('strip.next')}
          disabled={atEnd} onClick={() => scrollByPage(1)}>
          <ChevronRight size={20} aria-hidden="true" />
        </button>
      )}
    </nav>
  )
}
