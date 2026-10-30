import { Link } from 'react-router-dom'
import CategoryArt from '../components/CategoryArt.jsx'
import { t } from '../i18n/t.js'
import { useFetch } from '../hooks/useFetch.js'
import './CategoriesPage.css'

export default function CategoriesPage() {
  const { data: categories, loading, error } = useFetch('/api/categories')

  return (
    <div className="container page">
      <h1>{t('nav.categories')}</h1>
      <p className="muted categories-intro">{t('categories.intro')}</p>

      {loading && <p className="muted">{t('common.loading')}</p>}
      {error && <p className="muted">{t('categories.error')}</p>}

      {categories && (
        <ul className="category-grid">
          {categories.map((category) => (
            <li key={category.id}>
              <Link to={`/books?category=${category.slug}`} className="category-tile">
                <span className="tile-art"><CategoryArt slug={category.slug} /></span>
                <span className="tile-text">
                  <strong>{t(`category.${category.slug}`, {}, category.name)}</strong>
                  {category.description && <span>{category.description}</span>}
                </span>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
