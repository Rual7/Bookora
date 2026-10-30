import { useEffect, useState } from 'react'
import { api } from '../api/client.js'

/**
 * Loads `path` once per change. Pass null to do nothing (for example while a search box is empty).
 * State is derived from the latest finished request, so no setState runs synchronously in the effect.
 */
export function useFetch(path) {
  const [result, setResult] = useState({ path: null, data: null, error: null })

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
  }, [path])

  const settled = result.path === path
  return {
    data: path && settled ? result.data : null,
    error: path && settled ? result.error : null,
    loading: Boolean(path) && !settled,
  }
}
