import { Moon, Sun } from 'lucide-react'
import { useTheme } from '../hooks/useTheme.js'

export default function ThemeToggle({ className = 'icon-btn', withLabel = false }) {
  const { theme, toggle } = useTheme()
  const dark = theme === 'dark'
  const label = dark ? 'Switch to light theme' : 'Switch to dark theme'
  const Icon = dark ? Sun : Moon

  return (
    <button type="button" className={className} onClick={toggle} aria-label={label} aria-pressed={dark}>
      <Icon size={22} strokeWidth={1.5} aria-hidden="true" />
      {withLabel && <span>{dark ? 'Light theme' : 'Dark theme'}</span>}
    </button>
  )
}
