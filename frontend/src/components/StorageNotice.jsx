import { useState } from 'react'
import { Cookie } from 'lucide-react'
import { t } from '../i18n/t.js'
import './StorageNotice.css'

const KEY = 'bookora-cookie-notice'

function alreadySeen() {
  try {
    return localStorage.getItem(KEY) === 'seen'
  } catch {
    return false
  }
}

/** We only use what the shop needs to work, so this is a notice with one button, not a choice. */
export default function StorageNotice() {
  const [visible, setVisible] = useState(() => !alreadySeen())

  function dismiss() {
    try {
      localStorage.setItem(KEY, 'seen')
    } catch {
      // The notice will simply show again next visit.
    }
    setVisible(false)
  }

  if (!visible) return null

  return (
    <section className="storage-notice" aria-labelledby="storage-title">
      <span className="storage-notice-icon">
        <Cookie size={28} strokeWidth={1.5} aria-hidden="true" />
      </span>
      <div className="storage-notice-body">
        <h2 id="storage-title">{t('storage.title')}</h2>
        <p>{t('storage.text')}</p>
      </div>
      <button type="button" className="btn btn-small" onClick={dismiss}>{t('storage.ok')}</button>
    </section>
  )
}
