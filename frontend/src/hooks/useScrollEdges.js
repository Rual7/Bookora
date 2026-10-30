import { useCallback, useEffect, useState } from 'react'

const NOT_SCROLLABLE = { atStart: true, atEnd: true, pageCount: 1, currentPage: 0 }

/**
 * For a horizontally scrolling row: tells whether it can still scroll left or right (to enable the arrow
 * buttons), how many "pages" of its width it has and which one is showing, and scrolls it by a page.
 * Call `update` from the row's onScroll; it also runs on resize and whenever `watch` changes.
 */
export function useScrollEdges(rowRef, watch) {
  const [state, setState] = useState(NOT_SCROLLABLE)

  const update = useCallback(() => {
    const row = rowRef.current
    if (!row || row.clientWidth === 0) return
    const maxScroll = row.scrollWidth - row.clientWidth
    const pageCount = Math.max(1, Math.ceil(row.scrollWidth / row.clientWidth - 0.05))
    setState({
      atStart: row.scrollLeft <= 2,
      atEnd: row.scrollLeft >= maxScroll - 2,
      pageCount,
      currentPage: maxScroll <= 0 ? 0 : Math.round((row.scrollLeft / maxScroll) * (pageCount - 1)),
    })
  }, [rowRef])

  useEffect(() => {
    update()
    window.addEventListener('resize', update)
    return () => window.removeEventListener('resize', update)
  }, [update, watch])

  function scrollByPage(direction) {
    const row = rowRef.current
    if (row) row.scrollBy({ left: direction * row.clientWidth * 0.8, behavior: 'smooth' })
  }

  function scrollToPage(index) {
    const row = rowRef.current
    if (!row) return
    const lastPage = Math.max(1, state.pageCount - 1)
    row.scrollTo({ left: (row.scrollWidth - row.clientWidth) * (index / lastPage), behavior: 'smooth' })
  }

  return { ...state, update, scrollByPage, scrollToPage }
}
