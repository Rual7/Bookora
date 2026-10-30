import { useState } from 'react'
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
    <section className="storage-notice" aria-label={t('storage.label')}>
      <p>{t('storage.text')}</p>
      <button type="button" className="btn" onClick={dismiss}>{t('storage.ok')}</button>
    </section>
  )
}
