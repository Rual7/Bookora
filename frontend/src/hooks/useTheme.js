import { useCallback, useState } from 'react'

const STORAGE_KEY = 'bookora-theme'

export function useTheme() {
  const [theme, setTheme] = useState(() => document.documentElement.dataset.theme ?? 'light')

  const toggle = useCallback(() => {
    const next = document.documentElement.dataset.theme === 'dark' ? 'light' : 'dark'
    document.documentElement.dataset.theme = next
    try {
      localStorage.setItem(STORAGE_KEY, next)
    } catch {
      // storage can be unavailable (private mode); the choice then lasts for this visit only
    }
    setTheme(next)
  }, [])

  return { theme, toggle }
}
