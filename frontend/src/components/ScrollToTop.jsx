import { useEffect } from 'react'
import { useLocation } from 'react-router-dom'

/** A new page, or a new search or filter in the address (like "E-books" in the menu), starts at the top. */
export default function ScrollToTop() {
  const { pathname, search } = useLocation()

  useEffect(() => {
    window.scrollTo(0, 0)
  }, [pathname, search])

  return null
}
