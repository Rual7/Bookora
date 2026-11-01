import { useCallback, useEffect, useRef, useState } from 'react'
import { api } from '../api/client.js'

const PAGE_SIZE = 10

/**
 * Loads a paged list a few items at a time. `basePath` is the URL without `page` and `size`; when it changes
 * the list starts again from the first page. Call `loadMore` when the end of the list comes into view.
 */
export function useInfiniteList(basePath) {
  const [state, setState] = useState({ key: null, items: [], total: 0, next: 1, done: true, error: null })
  const [loadingMore, setLoadingMore] = useState(false)
  const busy = useRef(false)

  const pageUrl = useCallback(
    (page) => `${basePath}${basePath.includes('?') ? '&' : '?'}page=${page}&size=${PAGE_SIZE}`,
    [basePath],
  )

  useEffect(() => {
    const controller = new AbortController()
    api
      .get(pageUrl(0), controller.signal)
      .then((data) =>
        setState({ key: basePath, items: data.content, total: data.totalElements, next: 1, done: data.totalPages <= 1, error: null }))
      .catch((error) => {
        if (error.name !== 'AbortError') setState({ key: basePath, items: [], total: 0, next: 1, done: true, error })
      })
    return () => controller.abort()
  }, [basePath, pageUrl])

  const current = state.key === basePath

  const loadMore = useCallback(async () => {
    if (!current || state.done || busy.current) return
    busy.current = true
    setLoadingMore(true)
    try {
      const data = await api.get(pageUrl(state.next))
      setState((previous) =>
        previous.key !== basePath
          ? previous
          : { ...previous, items: [...previous.items, ...data.content], next: previous.next + 1, done: previous.next + 1 >= data.totalPages })
    } catch (error) {
      setState((previous) => ({ ...previous, error, done: true }))
    } finally {
      busy.current = false
      setLoadingMore(false)
    }
  }, [basePath, current, pageUrl, state.done, state.next])

  return {
    items: current ? state.items : [],
    total: current ? state.total : 0,
    error: current ? state.error : null,
    loading: !current,
    loadingMore,
    done: current && state.done,
    loadMore,
  }
}

/**
 * Returns a ref callback for an element at the end of a list: `onVisible` runs when it scrolls into view
 * (inside `rootRef`'s scrolling box, or the window). It runs again after `watch` changes while the element
 * is still in view, so a short first page keeps loading until the space is filled.
 */
export function useOnVisible(onVisible, watch, rootRef) {
  const [node, setNode] = useState(null)

  useEffect(() => {
    if (!node) return
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) onVisible()
      },
      { root: rootRef?.current ?? null, rootMargin: '300px' },
    )
    observer.observe(node)
    return () => observer.disconnect()
  }, [node, onVisible, watch, rootRef])

  return setNode
}
