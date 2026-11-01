import { useCallback, useEffect, useState } from 'react'
import { api } from '../api/client.js'

/**
 * Loads `path` once per change. Pass null to do nothing (for example while a search box is empty).
 * State is derived from the latest finished request, so no setState runs synchronously in the effect.
 * `reload` fetches the same path again and keeps showing the old data until the new data arrives.
 */
export function useFetch(path) {
  const [result, setResult] = useState({ path: null, data: null, error: null })
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    if (!path) return
    const controller = new AbortController()
    api
      .get(path, controller.signal)
      .then((data) => setResult({ path, data, error: null }))
      .catch((error) => {
        if (error.name !== 'AbortError') setResult({ path, data: null, error })
      })
    return () => controller.abort()
  }, [path, attempt])

  const reload = useCallback(() => setAttempt((count) => count + 1), [])

  const settled = result.path === path
  return {
    data: path && settled ? result.data : null,
    error: path && settled ? result.error : null,
    loading: Boolean(path) && !settled,
    reload,
  }
}
