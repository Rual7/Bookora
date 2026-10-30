import { Link } from 'react-router-dom'
import CategoryArt from '../components/CategoryArt.jsx'
import { useFetch } from '../hooks/useFetch.js'
import './CategoriesPage.css'

export default function CategoriesPage() {
  const { data: categories, loading, error } = useFetch('/api/categories')

  return (
    <div className="container page">
      <h1>Categories</h1>
      <p className="muted categories-intro">Pick a genre and see every book in it.</p>

      {loading && <p className="muted">Loading...</p>}
      {error && <p className="muted">The categories could not be loaded. Please try again later.</p>}

      {categories && (
        <ul className="category-grid">
          {categories.map((category) => (
            <li key={category.id}>
              <Link to={`/books?category=${category.slug}`} className="category-tile">
                <span className="tile-art"><CategoryArt slug={category.slug} /></span>
                <span className="tile-text">
                  <strong>{category.name}</strong>
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
