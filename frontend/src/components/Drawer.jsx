import { useEffect, useRef } from 'react'
import { X } from 'lucide-react'
import './Drawer.css'

/**
 * Side panel on the right. It is a native <dialog>, so the browser handles Escape,
 * keeps the keyboard focus inside and returns it to the button that opened it.
 */
export default function Drawer({ open, title, onClose, children }) {
  const dialogRef = useRef(null)

  useEffect(() => {
    const dialog = dialogRef.current
    if (open && !dialog.open) dialog.showModal()
    if (!open && dialog.open) dialog.close()
  }, [open])

  // A click on the dark area outside the panel lands on the <dialog> element itself.
  function closeOnBackdropClick(event) {
    if (event.target === dialogRef.current) onClose()
  }

  return (
    <dialog ref={dialogRef} className="drawer" aria-label={title} onClose={onClose} onClick={closeOnBackdropClick}>
      <div className="drawer-panel">
        <div className="drawer-head">
          <h2>{title}</h2>
          <button type="button" className="icon-btn" onClick={onClose} aria-label={`Close ${title.toLowerCase()}`}>
            <X size={22} strokeWidth={1.5} aria-hidden="true" />
          </button>
        </div>
        {children}
      </div>
    </dialog>
  )
}
